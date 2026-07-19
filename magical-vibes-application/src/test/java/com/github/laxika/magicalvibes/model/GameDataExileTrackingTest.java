package com.github.laxika.magicalvibes.model;

import com.github.laxika.magicalvibes.service.exile.ExileService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class GameDataExileTrackingTest {

    private GameData gd;
    private UUID ownerId;
    private UUID sourcePermanentId;

    @BeforeEach
    void setUp() {
        ownerId = UUID.randomUUID();
        sourcePermanentId = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", ownerId, "Player1");
    }

    private Card card(String name) {
        Card c = new Card();
        c.setName(name);
        return c;
    }

    @Test
    @DisplayName("Exile entries default to face up (CR 406.3 exiles are face up unless stated)")
    void entriesDefaultToFaceUp() {
        Card plain = card("Plain");
        Card tracked = card("Tracked");
        gd.addToExile(ownerId, plain);
        gd.addToExile(ownerId, tracked, sourcePermanentId);

        assertThat(gd.findExiledCard(plain.getId()).faceDown()).isFalse();
        assertThat(gd.findExiledCard(tracked.getId()).faceDown()).isFalse();
        assertThat(new ExiledCardEntry(plain, ownerId, null).faceDown()).isFalse();
    }

    @Test
    @DisplayName("The four-argument addToExile records the face-down status")
    void fourArgAddToExileSetsFaceDown() {
        Card hidden = card("Hidden");
        gd.addToExile(ownerId, hidden, sourcePermanentId, true);

        ExiledCardEntry entry = gd.findExiledCard(hidden.getId());
        assertThat(entry.faceDown()).isTrue();
        assertThat(entry.sourcePermanentId()).isEqualTo(sourcePermanentId);
        assertThat(entry.ownerId()).isEqualTo(ownerId);
    }

    @Test
    @DisplayName("clearAllSourceTracking drops the source link but keeps the face-down status")
    void clearAllSourceTrackingPreservesFaceDown() {
        Card hidden = card("Hidden");
        Card shown = card("Shown");
        gd.addToExile(ownerId, hidden, sourcePermanentId, true);
        gd.addToExile(ownerId, shown, sourcePermanentId);

        gd.clearAllSourceTracking();

        ExiledCardEntry hiddenEntry = gd.findExiledCard(hidden.getId());
        assertThat(hiddenEntry.sourcePermanentId()).isNull();
        assertThat(hiddenEntry.faceDown()).isTrue();
        ExiledCardEntry shownEntry = gd.findExiledCard(shown.getId());
        assertThat(shownEntry.sourcePermanentId()).isNull();
        assertThat(shownEntry.faceDown()).isFalse();
    }

    @Test
    @DisplayName("ExileService.exileCardFaceDown records a face-down entry; exileCard stays face up")
    void exileServiceFaceDownVariant() {
        ExileService exileService = new ExileService();
        Card hidden = card("Hidden");
        Card shown = card("Shown");

        exileService.exileCardFaceDown(gd, ownerId, hidden, sourcePermanentId);
        exileService.exileCard(gd, ownerId, shown, sourcePermanentId);

        assertThat(gd.findExiledCard(hidden.getId()).faceDown()).isTrue();
        assertThat(gd.findExiledCard(shown.getId()).faceDown()).isFalse();
    }
}
