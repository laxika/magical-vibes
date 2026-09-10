package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CravenGiant;
import com.github.laxika.magicalvibes.cards.s.SkyshroudFalcon;
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

@CardUsed({TidalSurge.class, CravenGiant.class, SkyshroudFalcon.class})
class TidalSurgeTest extends BaseCardTest {

    private void castTidalSurge(List<UUID> targets) {
        harness.setHand(player1, List.of(new TidalSurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, targets);
        harness.passBothPriorities();
    }

    // ===== Taps three non-flying creatures =====

    @Test
    @DisplayName("Taps up to three target creatures without flying")
    void tapsThreeCreatures() {
        List<UUID> targetIds = List.of(
                harness.addToBattlefieldAndReturn(player2, new CravenGiant()).getId(),
                harness.addToBattlefieldAndReturn(player2, new CravenGiant()).getId(),
                harness.addToBattlefieldAndReturn(player2, new CravenGiant()).getId());

        castTidalSurge(targetIds);

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .allMatch(Permanent::isTapped);
    }

    // ===== Taps a single creature =====

    @Test
    @DisplayName("Can target only one creature")
    void tapsOneCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new CravenGiant());

        castTidalSurge(List.of(target.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(target.getId()))
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Can target a creature controlled by the caster")
    void tapsCreatureControlledByCaster() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new CravenGiant());

        castTidalSurge(List.of(target.getId()));

        assertThat(target.isTapped()).isTrue();
    }

    // ===== Cannot target a creature with flying =====

    @Test
    @DisplayName("Cannot target a creature with flying")
    void cannotTargetFlyer() {
        Permanent flyer = harness.addToBattlefieldAndReturn(player2, new SkyshroudFalcon());
        harness.setHand(player1, List.of(new TidalSurge()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(flyer.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot choose more than three targets")
    void cannotChooseMoreThanThreeTargets() {
        List<UUID> targetIds = List.of(
                harness.addToBattlefieldAndReturn(player2, new CravenGiant()).getId(),
                harness.addToBattlefieldAndReturn(player2, new CravenGiant()).getId(),
                harness.addToBattlefieldAndReturn(player2, new CravenGiant()).getId(),
                harness.addToBattlefieldAndReturn(player2, new CravenGiant()).getId());

        assertThatThrownBy(() -> castTidalSurge(targetIds))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Zero targets is legal =====

    @Test
    @DisplayName("Can be cast with no targets")
    void castWithNoTargets() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CravenGiant());

        castTidalSurge(List.of());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getId().equals(creature.getId()))
                .noneMatch(Permanent::isTapped);
    }
}
