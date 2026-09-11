package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TidalSurge.class, GrizzlyBears.class, WindDrake.class, Island.class})
class TidalSurgeTest extends BaseCardTest {

    private void prepareTidalSurge() {
        harness.setHand(player1, List.of(new TidalSurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void castTidalSurge(List<UUID> targets) {
        prepareTidalSurge();
        harness.castSorcery(player1, 0, targets);
        harness.passBothPriorities();
    }

    // ===== Taps three non-flying creatures =====

    @Test
    @DisplayName("Taps up to three target creatures without flying")
    void tapsThreeCreatures() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent third = addCreatureReady(player2, new GrizzlyBears());

        castTidalSurge(List.of(first.getId(), second.getId(), third.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allMatch(Permanent::isTapped);
    }

    // ===== Taps a single creature =====

    @Test
    @DisplayName("Can target only one creature")
    void tapsOneCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castTidalSurge(List.of(creature.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(creature.getId()))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void tapsOwnCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        castTidalSurge(List.of(creature.getId()));

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getId().equals(creature.getId()))
                .allMatch(Permanent::isTapped);
    }

    // ===== Cannot target a creature with flying =====

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetFlyer() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new WindDrake());
        prepareTidalSurge();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(flyer.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature without flying");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        Permanent island = harness.addToBattlefieldAndReturn(player2, new Island());
        prepareTidalSurge();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature without flying");
    }

    @Test
    @DisplayName("Cannot choose more than three targets")
    void cannotTargetMoreThanThreeCreatures() {
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        Permanent third = addCreatureReady(player2, new GrizzlyBears());
        Permanent fourth = addCreatureReady(player2, new GrizzlyBears());
        prepareTidalSurge();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Zero targets is legal =====

    @Test
    @DisplayName("Can be cast with no targets")
    void castWithNoTargets() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        castTidalSurge(List.of());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(creature.getId()))
                .noneMatch(Permanent::isTapped);
    }
}
