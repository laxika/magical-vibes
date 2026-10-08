package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WreckingOgre.class, GrizzlyBears.class})
class WreckingOgreTest extends BaseCardTest {

    @Test
    @DisplayName("Bloodrush gives target attacking creature +3/+3 and double strike")
    void bloodrushBoostsAttackingCreature() {
        harness.setHand(player1, List.of(new WreckingOgre()));
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isTrue();
        harness.assertInGraveyard(player1, "Wrecking Ogre");
    }

    @Test
    @DisplayName("The bloodrush boost and double strike wear off at end of turn")
    void bloodrushWearsOff() {
        harness.setHand(player1, List.of(new WreckingOgre()));
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateHandAbility(player1, 0, bears.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Bloodrush cannot target a creature that isn't attacking")
    void bloodrushRejectsNonAttackingCreature() {
        harness.setHand(player1, List.of(new WreckingOgre()));
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInHand(player1, "Wrecking Ogre");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
    }

    @Test
    @DisplayName("Bloodrush pays mana and discards the Ogre before resolving")
    void bloodrushPaysCostsImmediately() {
        harness.setHand(player1, List.of(new WreckingOgre()));
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WreckingOgre());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateHandAbility(player1, 0, attacker.getId());

        harness.assertInGraveyard(player1, "Wrecking Ogre");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(3);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(6);
    }

    @Test
    @DisplayName("Bloodrush requires two red mana even with five total mana")
    void bloodrushRejectsInsufficientRedMana() {
        harness.setHand(player1, List.of(new WreckingOgre()));
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new WreckingOgre());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Wrecking Ogre");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Bloodrush can target an opponent's attacking creature")
    void bloodrushCanTargetOpponentsAttacker() {
        harness.setHand(player1, List.of(new WreckingOgre()));
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new WreckingOgre());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(6);
        harness.assertInGraveyard(player1, "Wrecking Ogre");
    }

    @Test
    @DisplayName("Bloodrush does not resolve if its target stops attacking")
    void bloodrushRechecksAttackingRestriction() {
        harness.setHand(player1, List.of(new WreckingOgre()));
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateHandAbility(player1, 0, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attacker)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
        harness.assertInGraveyard(player1, "Wrecking Ogre");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Wrecking Ogre deals combat damage in both damage steps")
    void intrinsicDoubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new WreckingOgre());
        attacker.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("Bloodrush double strike makes the boosted attacker deal damage twice")
    void bloodrushDoubleStrikeDealsDamageTwice() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new WreckingOgre()));
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.addMana(player1, ManaColor.RED, 5);

        harness.activateHandAbility(player1, 0, attacker.getId());
        harness.passBothPriorities();
        resolveCombat();

        harness.assertLife(player2, 10);
    }
}
