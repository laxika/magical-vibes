package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InspiritTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Inspirit untaps and boosts the target creature")
    void untapsAndBoosts() {
        Permanent target = addTappedCreature(player2);
        harness.setHand(player1, List.of(new Inspirit()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Boost expires at end of turn")
    void boostExpiresAtEndOfTurn() {
        Permanent target = addTappedCreature(player2);
        harness.setHand(player1, List.of(new Inspirit()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        Permanent ownCreature = addTappedCreature(player1);
        harness.setHand(player1, List.of(new Inspirit()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(ownCreature.getPowerModifier()).isEqualTo(2);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addTappedCreature(player1);
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new Inspirit()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Inspirit fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addTappedCreature(player2);
        harness.setHand(player1, List.of(new Inspirit()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    private Permanent addTappedCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.tap();
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
