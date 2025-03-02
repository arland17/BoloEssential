package loyality.member.cafe.boloessentials.halaman_admin;

import android.app.AlertDialog;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.ProgressDialog;
import android.content.BroadcastReceiver;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.Typeface;
import android.graphics.drawable.ColorDrawable;
import android.hardware.usb.UsbDevice;
import android.hardware.usb.UsbManager;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.PopupMenu;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import android.app.DownloadManager;
import android.content.Context;
import android.net.Uri;
import android.os.Environment;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.content.FileProvider;

import com.acs.smartcard.Reader;
import com.acs.smartcard.ReaderException;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.collection.BuildConfig;

import org.apache.poi.ss.usermodel.Cell;
import org.apache.poi.ss.usermodel.Row;
import org.apache.poi.ss.usermodel.Sheet;
import org.apache.poi.ss.usermodel.Workbook;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;
import org.w3c.dom.Text;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import loyality.member.cafe.boloessentials.R;
import loyality.member.cafe.boloessentials.halaman_userandworker.LoadingScreenActivity;
import loyality.member.cafe.boloessentials.model.User;

public class UserAdminActivity extends AppCompatActivity {
    private Button btntambahUser, btnPrevPage, btnNextPage, btn1, btn2, btn3;
    private Dialog mDialog;
    private Dialog nfcDialog;
    private DatabaseReference mDatabase;
    private ProgressBar progressBar;
    private ProgressDialog progressDialog;
    private TableLayout tableLayout;
    private TextView tvPointUser, etTanggalLahir, tvAbsen, tvDashboard, tvTukarPoint, tvTukarHadiah, tvAdministrator, tvUser, tvHadiah;
    private RelativeLayout logout;
    private static final int REQUEST_WRITE_STORAGE = 112;
    private int currentPage = 1;
    private int totalPageCount;
    private static final int ITEMS_PER_PAGE = 9;
    private List<User> userList = new ArrayList<>();
    private UsbManager mManager;
    private Reader mReader;
    private PendingIntent mPermissionIntent;
    private static final String ACTION_USB_PERMISSION = "com.android.example.USB_PERMISSION";
    private static final String TAG = UserAdminActivity.class.getSimpleName();


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_user_admin);

        progressBar = findViewById(R.id.progressBar);
        progressBar.setVisibility(View.VISIBLE);
        progressBar.setVisibility(View.GONE);

        int textColor = getIntent().getIntExtra("textColorUser", R.color.brownAdmin);
        tvUser = findViewById(R.id.tvUser);
        tvUser.setTextColor(getResources().getColor(textColor));

        mManager = (UsbManager) getSystemService(Context.USB_SERVICE);
        mReader = new Reader(mManager);
        mPermissionIntent = PendingIntent.getBroadcast(this, 0, new Intent(ACTION_USB_PERMISSION), PendingIntent.FLAG_MUTABLE);
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_USB_PERMISSION);
        filter.addAction(UsbManager.ACTION_USB_DEVICE_DETACHED);
        registerReceiver(mReceiver, filter);

        btnPrevPage = findViewById(R.id.btnPrevious);
        btnNextPage = findViewById(R.id.btnNext);

        btn1 = findViewById(R.id.btn1);
        btn2 = findViewById(R.id.btn2);
        btn3 = findViewById(R.id.btn3);

        // Tambahkan OnClickListener untuk tombol pagination di dalam onCreate
        btn1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage = Integer.parseInt(btn1.getText().toString());
                displayPageData();
            }
        });

        btn2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage = Integer.parseInt(btn2.getText().toString());
                displayPageData();
            }
        });

        btn3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                currentPage = Integer.parseInt(btn3.getText().toString());
                displayPageData();
            }
        });


        tvPointUser = findViewById(R.id.tvPointUser);
        logout = findViewById(R.id.btnLogout);
        logout.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showPopupMenu(view);
            }
        });

        tvDashboard = findViewById(R.id.tvDashboard);
        tvDashboard.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoaderAndStartActivity(DashboardAdminActivity.class);
            }
        });

        tvAbsen = findViewById(R.id.tvAbsen);
        tvAbsen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoaderAndStartActivity(KaryawanAdminActivity.class);
            }
        });

        tvTukarPoint = findViewById(R.id.tvTukarPoint);
        tvTukarPoint.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoaderAndStartActivity(TukarPointAdminActivity.class);
            }
        });

        tvTukarHadiah = findViewById(R.id.tvTukarHadiah);
        tvTukarHadiah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoaderAndStartActivity(TukarHadiahAdminActivity.class);
            }
        });

        tvAdministrator = findViewById(R.id.tvAdministrator);
        tvAdministrator.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoaderAndStartActivity(AdministratorAdminActivity.class);
            }
        });

        tvHadiah = findViewById(R.id.tvHadiah);
        tvHadiah.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showLoaderAndStartActivity(HadiahAdminActivity.class);
            }
        });

        btntambahUser = findViewById(R.id.btnTambahUser);
        tableLayout = findViewById(R.id.tableLayout);
        mDialog = new Dialog(this);
        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");

        btntambahUser.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mDialog.setContentView(R.layout.modal_tambah_user_admin);
                mDialog.getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
                mDialog.show();

                TextView etNomorID = mDialog.findViewById(R.id.etNomorID);
                EditText etNama = mDialog.findViewById(R.id.etNama);
                EditText etEmail = mDialog.findViewById(R.id.etEmail);
                EditText etTelpon = mDialog.findViewById(R.id.etTelpon);
                TextView etTanggalLahir = mDialog.findViewById(R.id.etTanggalLahir);
                Button btnSubmit = mDialog.findViewById(R.id.btnSubmit);
                ProgressBar loader = new ProgressBar(UserAdminActivity.this);
                Button btnDate = mDialog.findViewById(R.id.btnDate);
                Button btnTambahID = mDialog.findViewById(R.id.btnTambahID);

                btnTambahID.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        nfcDialog = new Dialog(UserAdminActivity.this);
                        nfcDialog.setContentView(R.layout.modal_nfc);
                        nfcDialog.setCancelable(true);

                        TextView UID = nfcDialog.findViewById(R.id.UID);
                        Button btnAcceptUID = nfcDialog.findViewById(R.id.btnAcceptUID);

                        nfcDialog.show();

                        initializeReader(UID, btnAcceptUID);
                    }
                });


                btnDate.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View v) {
                        // Mendapatkan tanggal saat ini
                        final Calendar calendar = Calendar.getInstance();
                        int year = calendar.get(Calendar.YEAR);
                        int month = calendar.get(Calendar.MONTH);
                        int day = calendar.get(Calendar.DAY_OF_MONTH);

                        // Membuka dialog tanggal
                        DatePickerDialog datePickerDialog = new DatePickerDialog(
                                UserAdminActivity.this,
                                new DatePickerDialog.OnDateSetListener() {
                                    @Override
                                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                        // Menampilkan tanggal yang dipilih di EditText
                                        String selectedDate = dayOfMonth + "/" + (month + 1) + "/" + year;
                                        etTanggalLahir.setText(selectedDate);
                                    }
                                },
                                year, month, day);
                        datePickerDialog.show();
                    }
                });
                btnSubmit.setOnClickListener(new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        // Mengambil nilai dari EditText
                        String nomorID = etNomorID.getText().toString().trim();
                        String nama = etNama.getText().toString().trim();
                        String email = etEmail.getText().toString().trim();
                        String telpon = etTelpon.getText().toString().trim();
                        String tanggalLahir = etTanggalLahir.getText().toString().trim();

                        // Cek apakah semua field diisi
                        if (nomorID.isEmpty() || nama.isEmpty() || email.isEmpty() || telpon.isEmpty() || tanggalLahir.isEmpty()) {
                            Toast.makeText(UserAdminActivity.this, "Semua field harus diisi", Toast.LENGTH_SHORT).show();
                            return;
                        }

                        // Cek apakah nomorID sudah ada di database
                        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference("users");
                        userRef.orderByChild("nomorID").equalTo(nomorID).addListenerForSingleValueEvent(new ValueEventListener() {
                            @Override
                            public void onDataChange(DataSnapshot dataSnapshot) {
                                if (dataSnapshot.exists()) {
                                    // Jika nomorID sudah ada, tampilkan alert dan reset field
                                    Toast.makeText(UserAdminActivity.this, "ID sudah digunakan, gunakan id lain", Toast.LENGTH_SHORT).show();
                                    etNomorID.setText(""); // Mengosongkan field nomorID
                                } else {
                                    // Jika nomorID belum ada, lanjutkan dengan penambahan user
                                    loader.setVisibility(View.VISIBLE);

                                    // Format tanggal bergabung
                                    String tanggalBergabung = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault()).format(new Date());
                                    int pointUser = 0;

                                    // Membuat objek User
                                    User user = new User(nomorID, nama, tanggalBergabung, email, telpon, tanggalLahir, pointUser);
                                    databaseReference.push().setValue(user).addOnCompleteListener(task -> {
                                        loader.setVisibility(View.GONE);
                                        if (task.isSuccessful()) {
                                            Toast.makeText(UserAdminActivity.this, "User berhasil ditambahkan", Toast.LENGTH_SHORT).show();
                                            mDialog.dismiss();
                                        } else {
                                            Toast.makeText(UserAdminActivity.this, "Gagal menambahkan user", Toast.LENGTH_SHORT).show();
                                        }
                                    });
                                }
                            }

                            @Override
                            public void onCancelled(DatabaseError databaseError) {
                                // Handle possible errors
                                Toast.makeText(UserAdminActivity.this, "Gagal memeriksa ID", Toast.LENGTH_SHORT).show();
                            }
                        });
                    }
                });


            }
        });

        tableLayout.setVisibility(View.GONE);

        btnPrevPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage > 1) {
                    currentPage--;
                    displayPageData();
                    updatePaginationButtons();
                }
            }
        });

        btnNextPage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (currentPage < totalPageCount) {
                    currentPage++;
                    displayPageData();
                    updatePaginationButtons();
                }
            }
        });

        databaseReference.orderByChild("tanggalBergabung").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                tableLayout.removeAllViews();
                addTableHeader();

                userList.clear();
                int userCount = 0;

                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user);
                        userCount++;
                    }
                }

                tvPointUser.setText(String.valueOf(userCount));

                Collections.reverse(userList);
                totalPageCount = (int) Math.ceil((double) userList.size() / ITEMS_PER_PAGE);

                currentPage = 1;
                displayPageData();
                updatePaginationButtons();

                progressBar.setVisibility(View.GONE);
                tableLayout.setVisibility(View.VISIBLE);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(UserAdminActivity.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                progressBar.setVisibility(View.GONE);
            }
        });
        setupExportButton();
    }

    @Override
    protected void onResume() {
        super.onResume(); // Pastikan NFC Reader diinisialisasi ketika UserAdminActivity aktif
    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mReader != null) {
            mReader.close();  // Pastikan NFC Reader ditutup saat UserAdminActivity tidak aktif
        }
    }

    private void initializeReader(TextView UID, Button btnAcceptUID) {
        if (mReader != null) {
            for (UsbDevice device : mManager.getDeviceList().values()) {
                if (mReader.isSupported(device)) {
                    if (mManager.hasPermission(device)) {
                        new OpenTask().execute(device);
                    } else {
                        mManager.requestPermission(device, mPermissionIntent);
                    }
                }
            }

            mReader.setOnStateChangeListener((slotNum, prevState, currState) -> {
                if (currState == Reader.CARD_PRESENT) {
                    Log.d(TAG, "NFC tag detected. Ready to read...");

                    final byte[] command = {(byte) 0xFF, (byte) 0xCA, (byte) 0x00, (byte) 0x00, (byte) 0x00};
                    final byte[] response = new byte[256];

                    try {
                        int byteCount = mReader.control(slotNum, Reader.IOCTL_CCID_ESCAPE,
                                command, command.length, response, response.length);

                        StringBuilder uid = new StringBuilder();
                        for (int i = 0; i < (byteCount - 2); i++) {
                            uid.append(String.format("%02X", response[i]));
                        }

                        Log.d(TAG, "Detected NFC UID: " + uid.toString());

                        runOnUiThread(() -> {
                            Long result = Long.parseLong(uid.toString(), 16);
                            UID.setText(String.valueOf(result));
                        });

                    } catch (ReaderException | NumberFormatException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> Toast.makeText(getApplicationContext(), "NFC tag read failed. Please try again.", Toast.LENGTH_LONG).show());
                    }
                }
            });

            btnAcceptUID.setOnClickListener(v -> {
                if (nfcDialog != null && nfcDialog.isShowing()) {
                    // Mengisi nilai dari TextView UID ke dalam EditText di dialog utama
                    TextView etNomorID = mDialog.findViewById(R.id.etNomorID);
                    if (etNomorID != null) {
                        etNomorID.setText(UID.getText().toString());
                    }
                    nfcDialog.dismiss(); // Menutup dialog NFC
                }
            });
        }
    }


    private final BroadcastReceiver mReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {
            String action = intent.getAction();

            if (ACTION_USB_PERMISSION.equals(action)) {
                synchronized (this) {
                    UsbDevice device = intent.getParcelableExtra(UsbManager.EXTRA_DEVICE);

                    if (intent.getBooleanExtra(UsbManager.EXTRA_PERMISSION_GRANTED, false)) {
                        if (device != null) {
                            new OpenTask().execute(device);
                        }
                    }
                }
            } else if (UsbManager.ACTION_USB_DEVICE_DETACHED.equals(action)) {
                synchronized (this) {
                    mReader.close();
                }
            }
        }
    };

    private class OpenTask extends AsyncTask<UsbDevice, Void, Exception> {
        @Override
        protected Exception doInBackground(UsbDevice... params) {
            Exception result = null;
            try {
                mReader.open(params[0]);
            } catch (Exception e) {
                result = e;
            }
            return result;
        }

        @Override
        protected void onPostExecute(Exception result) {
            if (result != null) {
                Toast.makeText(getApplicationContext(), "Error opening NFC reader", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void displayPageData() {
        tableLayout.removeViews(1, tableLayout.getChildCount() - 1);

        int startIndex = (currentPage - 1) * ITEMS_PER_PAGE;
        int endIndex = Math.min(startIndex + ITEMS_PER_PAGE, userList.size());

        for (int i = startIndex; i < endIndex; i++) {
            addUserRow(userList.get(i));
        }

        updatePaginationButtons();
    }

    @Override
    public void onBackPressed() {
        // Memicu showPopupMenu saat tombol back ditekan
        showPopupMenu(logout);
    }

    private void addTableHeader() {
        TableRow headerRow = new TableRow(this);
        String[] headers = {"Nama User", "Tanggal Bergabung", "Email", "No Telepon", "Tanggal Lahir", "Jumlah Point"};
        float[] weights = {1.3f, 0.7f, 2f, 1f, 1f, 0.6f};

        for (int i = 0; i < headers.length; i++) {
            TextView textView = new TextView(this);
            textView.setText(headers[i]);
            textView.setTextColor(getResources().getColor(R.color.white));
            textView.setTextSize(12);
            textView.setGravity(Gravity.CENTER);
            textView.setTypeface(null, Typeface.BOLD);
            textView.setPadding(5, 5, 5, 5);

            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    weights[i]
            );
            textView.setLayoutParams(params);
            headerRow.addView(textView);
        }
        headerRow.setBackgroundColor(getResources().getColor(R.color.brownAdmin));
        tableLayout.addView(headerRow);
    }

    private void addUserRow(User user) {
        TableRow row = new TableRow(this);
        String[] userData = {
                user.getNama(),
                formatTanggalBergabung(user.getTanggalBergabung()),
                user.getEmail(),
                user.getTelpon(),
                formatTanggalLahir(user.getTanggalLahir()),
                String.valueOf(user.getPointUser())
        };

        float[] weights = {1.3f, 0.7f, 2f, 1f, 1f, 0.6f};

        for (int i = 0; i < userData.length; i++) {
            TextView textView = new TextView(this);
            textView.setText(userData[i]);
            textView.setGravity(Gravity.CENTER);
            textView.setTextSize(12);
            textView.setPadding(5, 3, 5, 3);

            TableRow.LayoutParams params = new TableRow.LayoutParams(
                    0,
                    TableRow.LayoutParams.WRAP_CONTENT,
                    weights[i]
            );
            textView.setLayoutParams(params);
            row.addView(textView);
        }
        tableLayout.addView(row);
    }

    private String formatTanggalBergabung(String tanggalBergabung) {
        SimpleDateFormat inputFormat = new SimpleDateFormat("yyyy-MM-dd", Locale.getDefault());
        SimpleDateFormat outputFormat = new SimpleDateFormat("dd-MM-yyyy", Locale.getDefault());
        try {
            Date date = inputFormat.parse(tanggalBergabung);
            return outputFormat.format(date);
        } catch (ParseException e) {
            e.printStackTrace();
            return tanggalBergabung;
        }
    }

    private String formatTanggalLahir(String tanggalLahir) {
        if (tanggalLahir != null && tanggalLahir.length() == 8) {
            String day = tanggalLahir.substring(0, 2);
            String month = tanggalLahir.substring(2, 4);
            String year = tanggalLahir.substring(4, 8);
            return day + "-" + month + "-" + year;
        } else {
            return tanggalLahir;
        }
    }

    private void updatePaginationButtons() {
        // Reset semua tombol ke warna default
        btn1.setBackgroundTintList(getResources().getColorStateList(R.color.gray));
        btn2.setBackgroundTintList(getResources().getColorStateList(R.color.gray));
        btn3.setBackgroundTintList(getResources().getColorStateList(R.color.gray));

        btn1.setTextColor(getResources().getColor(R.color.black));
        btn2.setTextColor(getResources().getColor(R.color.black));
        btn3.setTextColor(getResources().getColor(R.color.black));

        if (totalPageCount == 0) {
            btnNextPage.setVisibility((View.INVISIBLE));
            btnPrevPage.setVisibility((View.INVISIBLE));
            btn1.setVisibility(View.INVISIBLE);
            btn2.setVisibility(View.INVISIBLE);
            btn3.setVisibility(View.INVISIBLE);
        } else if (totalPageCount == 1) {
            btn1.setText("1");
            btn2.setVisibility(View.INVISIBLE);
            btn3.setVisibility(View.INVISIBLE);
        } else if (totalPageCount == 2) {
            btn1.setText("1");
            btn2.setText("2");
            btn2.setVisibility(View.VISIBLE);
            btn3.setVisibility(View.INVISIBLE);
        } else {
            // Mengatur teks tombol berdasarkan halaman saat ini
            if (currentPage == 1) {
                btn1.setText("1");
                btn2.setText("2");
                btn3.setText("3");
            } else if (currentPage == totalPageCount) {
                btn1.setText(String.valueOf(totalPageCount - 2));
                btn2.setText(String.valueOf(totalPageCount - 1));
                btn3.setText(String.valueOf(totalPageCount));
            } else {
                btn1.setText(String.valueOf(currentPage - 1));
                btn2.setText(String.valueOf(currentPage));
                btn3.setText(String.valueOf(currentPage + 1));
            }

            btn2.setVisibility(View.VISIBLE);
            btn3.setVisibility(View.VISIBLE);
        }

        // Menandai tombol aktif dengan warna brownAdmin
        if (currentPage == Integer.parseInt(btn1.getText().toString())) {
            btn1.setBackgroundTintList(getResources().getColorStateList(R.color.brownAdmin));
            btn1.setTextColor(getResources().getColor(R.color.white));
        } else if (currentPage == Integer.parseInt(btn2.getText().toString())) {
            btn2.setBackgroundTintList(getResources().getColorStateList(R.color.brownAdmin));
            btn2.setTextColor(getResources().getColor(R.color.white));
        } else if (currentPage == Integer.parseInt(btn3.getText().toString())) {
            btn3.setBackgroundTintList(getResources().getColorStateList(R.color.brownAdmin));
            btn3.setTextColor(getResources().getColor(R.color.white));
        }
    }


    private void showPopupMenu(View view) {
        PopupMenu popupMenu = new PopupMenu(this, view);
        popupMenu.inflate(R.menu.menu_dropdown);
        popupMenu.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                Intent intent = new Intent(UserAdminActivity.this, LoadingScreenActivity.class);
                startActivity(intent);
                Toast.makeText(UserAdminActivity.this, "Logout clicked", Toast.LENGTH_SHORT).show();
                return true;
            }
        });
        popupMenu.show();
    }

    // Metode untuk menampilkan loader dan memulai aktivitas
    private void showLoaderAndStartActivity(final Class<?> targetActivity) {
        progressBar.setVisibility(View.VISIBLE);
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                Intent intent = new Intent(UserAdminActivity.this, targetActivity);
                startActivity(intent);
                progressBar.setVisibility(View.GONE);
            }
        }, 500);
    }

    private void setupExportButton() {
        Button btnExport = findViewById(R.id.btnExport);
        btnExport.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                showExportConfirmationDialog();
            }
        });
    }

    private void showExportConfirmationDialog() {
        // Dialog untuk konfirmasi export
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Konfirmasi Export");
        builder.setMessage("Apakah Anda ingin melakukan export data?");

        // Jika user klik "Ya"
        builder.setPositiveButton("Ya", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                showFileNameInputDialog();
            }
        });

        // Jika user klik "Tidak"
        builder.setNegativeButton("Tidak", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.dismiss();
            }
        });

        // Tampilkan dialog konfirmasi
        builder.create().show();
    }

    private void showFileNameInputDialog() {
        // Membuat dialog input untuk meminta nama file dari user
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Nama File");

        // EditText untuk user input
        final EditText input = new EditText(this);
        input.setHint("Masukkan nama file");
        builder.setView(input);

        // Jika user klik "Simpan"
        builder.setPositiveButton("Simpan", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                String fileName = input.getText().toString().trim();
                if (!fileName.isEmpty()) {
                    exportDataToExcel(fileName);
                } else {
                    Toast.makeText(UserAdminActivity.this, "Nama file tidak boleh kosong", Toast.LENGTH_SHORT).show();
                }
            }
        });

        // Jika user klik "Batal"
        builder.setNegativeButton("Batal", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                dialog.cancel();
            }
        });

        // Tampilkan dialog input nama file
        builder.create().show();
    }

    private void exportDataToExcel(String fileName) {
        progressDialog = new ProgressDialog(UserAdminActivity.this);
        progressDialog.setTitle("Exporting Data");
        progressDialog.setMessage("Please wait while the data is being exported to an Excel file...");
        progressDialog.setCancelable(false);
        progressDialog.show();

        DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("users");
        databaseReference.orderByChild("tanggalBergabung").addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                List<User> userList = new ArrayList<>();
                for (DataSnapshot dataSnapshot : snapshot.getChildren()) {
                    User user = dataSnapshot.getValue(User.class);
                    if (user != null) {
                        userList.add(user);
                    }
                }

                new Thread(() -> {
                    try {
                        File file = createExcelFile(userList);
                        if (file != null) {
                            saveFileToDownloads(file, fileName);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        runOnUiThread(() -> {
                            Toast.makeText(UserAdminActivity.this, "Failed to export data", Toast.LENGTH_SHORT).show();
                        });
                    } finally {
                        runOnUiThread(() -> {
                            if (progressDialog.isShowing()) {
                                progressDialog.dismiss();
                            }
                        });
                    }
                }).start();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                runOnUiThread(() -> {
                    Toast.makeText(UserAdminActivity.this, "Failed to fetch data: " + error.getMessage(), Toast.LENGTH_SHORT).show();
                    if (progressDialog.isShowing()) {
                        progressDialog.dismiss();
                    }
                });
            }
        });
    }

    private void saveFileToDownloads(File file, String fileName) {
        new Thread(() -> {
            try {
                File downloadsDir = Environment.getExternalStoragePublicDirectory(Environment.DIRECTORY_DOWNLOADS);
                File destFile = new File(downloadsDir, fileName + ".xlsx"); // Menggunakan nama file dari user
                try (FileInputStream inStream = new FileInputStream(file);
                     FileOutputStream outStream = new FileOutputStream(destFile)) {
                    byte[] buffer = new byte[1024];
                    int length;
                    while ((length = inStream.read(buffer)) > 0) {
                        outStream.write(buffer, 0, length);
                    }
                }

                runOnUiThread(() -> {
                    Toast.makeText(UserAdminActivity.this, "File berhasil disimpan di Downloads", Toast.LENGTH_SHORT).show();
                });
            } catch (IOException e) {
                e.printStackTrace();
                runOnUiThread(() -> {
                    Toast.makeText(UserAdminActivity.this, "Gagal menyimpan file", Toast.LENGTH_SHORT).show();
                });
            }
        }).start();
    }


    // Helper method to create an Excel file
    private File createExcelFile(List<User> userList) throws IOException {
        Workbook workbook = new XSSFWorkbook();
        Sheet sheet = workbook.createSheet("Users");

        // Create header row
        Row headerRow = sheet.createRow(0);
        String[] headers = {"Nama User", "Tanggal Bergabung", "Email", "No Telepon", "Tanggal Lahir", "Jumlah Point"};
        int cellIndex = 0;
        for (String header : headers) {
            Cell cell = headerRow.createCell(cellIndex++);
            cell.setCellValue(header);
        }

        // Create data rows
        int rowIndex = 1;
        for (User user : userList) {
            Row row = sheet.createRow(rowIndex++);
            row.createCell(0).setCellValue(user.getNama());
            row.createCell(1).setCellValue(formatTanggalBergabung(user.getTanggalBergabung()));
            row.createCell(2).setCellValue(user.getEmail());
            row.createCell(3).setCellValue(user.getTelpon());
            row.createCell(4).setCellValue(formatTanggalLahir(user.getTanggalLahir()));
            row.createCell(5).setCellValue(user.getPointUser());
        }

        // Write the output to a file
        File file = new File(getExternalFilesDir(null), "UsersData.xlsx");
        try (FileOutputStream fileOut = new FileOutputStream(file)) {
            workbook.write(fileOut);
        }
        workbook.close();
        return file;
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions,
                                           @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == REQUEST_WRITE_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                // Permission granted
            } else {
                // Permission denied
                Toast.makeText(this, "Permission denied. Unable to save file.", Toast.LENGTH_SHORT).show();
            }
        }
    }
}
