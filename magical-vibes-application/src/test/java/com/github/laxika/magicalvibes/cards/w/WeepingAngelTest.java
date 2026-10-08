package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.e.EnchantedEvening;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeepingAngel.class, GrizzlyBears.class, MindStone.class, Shock.class, EnchantedEvening.class})
class WeepingAngelTest extends BaseCardTest {

    @Test
    @DisplayName("An opponent casting a creature spell makes Weeping Angel a noncreature artifact until end of turn")
    void opponentCreatureSpellTurnsAngelIntoArtifact() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new WeepingAngel());

        opponentCasts(new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, angel)).isFalse();
        assertThat(gqs.isArtifact(gd, angel)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, angel)).isTrue();
    }

    @Test
    @DisplayName("A noncreature spell does not turn Weeping Angel into a statue")
    void noncreatureSpellDoesNotTurnAngelIntoArtifactOnly() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new WeepingAngel());

        opponentCasts(new MindStone(), "{2}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, angel)).isTrue();
    }

    @Test
    @DisplayName("Combat damage from Weeping Angel shuffles the damaged creature instead of dealing damage")
    void combatDamageShufflesDamagedCreature() {
        Permanent angel = addAttacker(player1);
        Permanent blocker = addBlockerWithPowerAndToughness(player2, 0, 3);
        harness.setLibrary(player2, List.of(new MindStone()));

        resolveWeepingAngelCombat();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(blocker.getId()));
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
        assertThat(angel.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("The shuffle replacement does not apply to noncombat damage")
    void noncombatDamageIsNotReplaced() {
        Card angelCard = new WeepingAngel();
        angelCard.setToughness(3);
        Permanent angel = harness.addToBattlefieldAndReturn(player2, angelCard);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, angel.getId());

        assertThat(angel.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Combat damage that cannot be prevented still shuffles the damaged creature")
    void unpreventableCombatDamageStillShuffles() {
        addAttacker(player1);
        Permanent blocker = addBlockerWithPowerAndToughness(player2, 0, 3);
        harness.setLibrary(player2, List.of(new MindStone()));
        gd.damageCantBePreventedThisTurn = true;

        resolveWeepingAngelCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Casting your own creature spell does not make Weeping Angel a noncreature")
    void ownCreatureSpellDoesNotRemoveCreatureType() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new WeepingAngel());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, angel)).isTrue();
    }

    @Test
    @DisplayName("Weeping Angel retains other card types when it stops being a creature")
    void retainsEnchantmentTypeWhenBecomingNoncreature() {
        Permanent angel = harness.addToBattlefieldAndReturn(player1, new WeepingAngel());
        harness.addToBattlefield(player1, new EnchantedEvening());
        assertThat(gqs.isEnchantment(gd, angel)).isTrue();

        opponentCasts(new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, angel)).isFalse();
        assertThat(gqs.isArtifact(gd, angel)).isTrue();
        assertThat(gqs.isEnchantment(gd, angel)).isTrue();
    }

    @Test
    @DisplayName("A blocking Weeping Angel shuffles an attacker before its normal combat damage")
    void blockingAngelShufflesAttackerBeforeNormalDamage() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent angel = harness.addToBattlefieldAndReturn(player2, new WeepingAngel());
        angel.setBlocking(true);
        angel.addBlockingTarget(0);
        harness.setLibrary(player1, List.of(new MindStone()));

        resolveWeepingAngelCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerDecks.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(angel);
        assertThat(angel.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Unblocked Weeping Angel deals combat damage to the defending player")
    void unblockedAngelDealsDamageToPlayer() {
        addAttacker(player1);

        resolveWeepingAngelCombat();

        harness.assertLife(player2, 18);
    }
    private void opponentCasts(Card card, String manaCost) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player2, card, manaCost);
    }

    private Permanent addAttacker(Player player) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player, new WeepingAngel());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        return attacker;
    }

    private Permanent addBlockerWithPowerAndToughness(
            Player player, int power, int toughness) {
        Card blockerCard = new GrizzlyBears();
        blockerCard.setPower(power);
        blockerCard.setToughness(toughness);
        Permanent blocker = harness.addToBattlefieldAndReturn(player, blockerCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        return blocker;
    }

    private void resolveWeepingAngelCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
