package com.github.laxika.magicalvibes.cards.s;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.mockStatic;
import static org.mockito.Mockito.when;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Millstone;
import com.github.laxika.magicalvibes.cards.w.WakeThrasher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ThreadLocalRandom;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({Scrambleverse.class, Forest.class, GrizzlyBears.class, HillGiant.class,
        Millstone.class, SongOfTheDryads.class, WakeThrasher.class})
class ScrambleverseTest extends BaseCardTest {

    private void castScrambleverse() {
        harness.setHand(player1, List.of(new Scrambleverse()));
        harness.addMana(player1, ManaColor.RED, 8);
        harness.castAndResolveSorcery(player1, 0, 0);
    }

    private UUID controllerOf(UUID permanentId) {
        return gd.playerBattlefields.entrySet().stream()
                .filter(e -> e.getValue().stream().anyMatch(p -> p.getId().equals(permanentId)))
                .map(java.util.Map.Entry::getKey)
                .findFirst()
                .orElse(null);
    }

    @Test
    @DisplayName("Every nonland permanent ends up controlled by one of the players and untapped")
    void redistributesAndUntapsNonlands() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player2, new HillGiant());
        Permanent millstone = harness.addToBattlefieldAndReturn(player2, new Millstone());
        bears.tap();
        giant.tap();
        millstone.tap();

        castScrambleverse();

        for (Permanent permanent : List.of(bears, giant, millstone)) {
            assertThat(controllerOf(permanent.getId()))
                    .isIn(player1.getId(), player2.getId());
            assertThat(permanent.isTapped()).isFalse();
        }
    }

    @Test
    @DisplayName("Lands are untouched — controller and tapped state stay as they were")
    void landsAreUnaffected() {
        Permanent myForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent theirForest = harness.addToBattlefieldAndReturn(player2, new Forest());
        theirForest.tap();

        castScrambleverse();

        assertThat(controllerOf(myForest.getId())).isEqualTo(player1.getId());
        assertThat(controllerOf(theirForest.getId())).isEqualTo(player2.getId());
        assertThat(theirForest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Redistribution preserves every permanent regardless of the random split")
    void redistributionPreservesAllPermanents() {
        List<UUID> ids = new java.util.ArrayList<>();
        for (int i = 0; i < 30; i++) {
            ids.add(harness.addToBattlefieldAndReturn(player1, new GrizzlyBears()).getId());
        }

        castScrambleverse();

        assertThat(gd.playerBattlefields.values().stream()
                .flatMap(List::stream).map(Permanent::getId).toList())
                .containsExactlyInAnyOrderElementsOf(ids);
    }

    @Test
    @DisplayName("A permanent turned into a land is neither redistributed nor untapped")
    void enchantedForestIsUnaffected() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SongOfTheDryads()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();
        assertThat(gqs.isLand(gd, bears)).isTrue();
        bears.tap();

        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextInt(2)).thenReturn(new java.util.ArrayList<>(gd.playerIds).indexOf(player2.getId()));
        try (var choices = mockStatic(ThreadLocalRandom.class)) {
            choices.when(ThreadLocalRandom::current).thenReturn(random);
            castScrambleverse();
        }

        assertThat(controllerOf(bears.getId())).isEqualTo(player1.getId());
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("All control changes finish before untap triggers are checked")
    void controlChangesPrecedeUntaps() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent thrasher = harness.addToBattlefieldAndReturn(player1, new WakeThrasher());
        bears.tap();
        thrasher.tap();

        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextInt(2)).thenReturn(new java.util.ArrayList<>(gd.playerIds).indexOf(player2.getId()));
        try (var choices = mockStatic(ThreadLocalRandom.class)) {
            choices.when(ThreadLocalRandom::current).thenReturn(random);
            castScrambleverse();
        }

        assertThat(controllerOf(bears.getId())).isEqualTo(player2.getId());
        assertThat(controllerOf(thrasher.getId())).isEqualTo(player2.getId());
        assertThat(gd.stack).hasSize(2);
    }

    @Test
    @DisplayName("Permanents untap even when their randomly chosen controller is unchanged")
    void unchangedControllersStillUntap() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        bears.tap();
        ThreadLocalRandom random = mock(ThreadLocalRandom.class);
        when(random.nextInt(2)).thenReturn(new java.util.ArrayList<>(gd.playerIds).indexOf(player1.getId()));
        try (var choices = mockStatic(ThreadLocalRandom.class)) {
            choices.when(ThreadLocalRandom::current).thenReturn(random);
            castScrambleverse();
        }
        assertThat(controllerOf(bears.getId())).isEqualTo(player1.getId());
        assertThat(bears.isTapped()).isFalse();
    }
}
