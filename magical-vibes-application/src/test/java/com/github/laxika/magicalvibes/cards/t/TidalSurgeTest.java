package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
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
        harness.castAndResolveSorcery(player1, 0, targets);
    }

    @Test
    @DisplayName("Taps up to three target creatures without flying")
    void tapsThreeCreatures() {
        List<UUID> targetIds = List.of(
                harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId(),
                harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId(),
                harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId());

        castTidalSurge(targetIds);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Can target only one creature")
    void tapsOneCreature() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        castTidalSurge(List.of(targetId));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(targetId))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetFlyer() {
        UUID flyerId = harness.addToBattlefieldAndReturn(player2, new WindDrake()).getId();
        prepareTidalSurge();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(flyerId)))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast with no targets")
    void castWithNoTargets() {
        UUID bearsId = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        castTidalSurge(List.of());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(bearsId))
                .noneMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Cannot target more than three creatures")
    void cannotTargetMoreThanThreeCreatures() {
        List<UUID> targetIds = List.of(
                harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId(),
                harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId(),
                harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId(),
                harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId());
        prepareTidalSurge();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, targetIds))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        UUID islandId = harness.addToBattlefieldAndReturn(player2, new Island()).getId();
        prepareTidalSurge();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(islandId)))
                .isInstanceOf(IllegalStateException.class);
    }
}
