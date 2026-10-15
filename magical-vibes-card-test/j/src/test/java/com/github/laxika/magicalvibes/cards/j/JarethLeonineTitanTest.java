package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.cards.h.HighGround;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JarethLeonineTitan.class, ElvishWarrior.class, HighGround.class})
class JarethLeonineTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking gives Jareth +7/+7 until end of turn")
    void blockingBoostsJareth() {
        Permanent jareth = addCreatureReady(player1, new JarethLeonineTitan());
        Permanent attacker = addCreatureReady(player2, new ElvishWarrior());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(jareth.getEffectivePower()).isEqualTo(11);
        assertThat(jareth.getEffectiveToughness()).isEqualTo(14);
    }

    @Test
    @DisplayName("Jareth's blocking boost wears off at end of turn")
    void blockingBoostWearsOffAtEndOfTurn() {
        Permanent jareth = addCreatureReady(player1, new JarethLeonineTitan());
        Permanent attacker = addCreatureReady(player2, new ElvishWarrior());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(jareth.getEffectivePower()).isEqualTo(11);
        assertThat(jareth.getEffectiveToughness()).isEqualTo(14);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(jareth.getEffectivePower()).isEqualTo(4);
        assertThat(jareth.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    @DisplayName("Blocking multiple creatures gives Jareth +7/+7 only once")
    void blockingMultipleCreaturesBoostsJarethOnlyOnce() {
        harness.addToBattlefield(player1, new HighGround());
        Permanent jareth = addCreatureReady(player1, new JarethLeonineTitan());
        Permanent firstAttacker = addCreatureReady(player2, new ElvishWarrior());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player2, new ElvishWarrior());
        secondAttacker.setAttacking(true);

        List<Permanent> defenderBattlefield = gd.playerBattlefields.get(player1.getId());
        List<Permanent> attackerBattlefield = gd.playerBattlefields.get(player2.getId());
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(
                new BlockerAssignment(defenderBattlefield.indexOf(jareth), attackerBattlefield.indexOf(firstAttacker)),
                new BlockerAssignment(defenderBattlefield.indexOf(jareth), attackerBattlefield.indexOf(secondAttacker))));
        resolveAllTriggers();

        assertThat(jareth.getEffectivePower()).isEqualTo(11);
        assertThat(jareth.getEffectiveToughness()).isEqualTo(14);
    }

    @Test
    @DisplayName("Jareth gains the chosen protection until end of turn")
    void gainsProtectionOfChosenColor() {
        Permanent jareth = addCreatureReady(player1, new JarethLeonineTitan());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, CardColor.RED.name());

        assertThat(jareth.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Jareth's chosen protection wears off at end of turn")
    void protectionWearsOffAtEndOfTurn() {
        Permanent jareth = addCreatureReady(player1, new JarethLeonineTitan());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.BLUE.name());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(jareth.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.BLUE);
    }

    @Test
    @DisplayName("Repeated activations retain protection from both chosen colors even while tapped and summoning sick")
    void repeatedActivationsRetainBothProtections() {
        Permanent jareth = addCreatureReady(player1, new JarethLeonineTitan());
        jareth.tap();
        jareth.setSummoningSick(true);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.RED.name());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.GREEN.name());

        assertThat(jareth.getProtectionFromColorsUntilEndOfTurn())
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.GREEN);
    }

    @Test
    @DisplayName("Protection from green prevents combat damage without stopping Jareth from blocking")
    void protectionPreventsCombatDamageFromChosenColor() {
        Permanent jareth = addCreatureReady(player1, new JarethLeonineTitan());
        addCreatureReady(player2, new ElvishWarrior());
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardColor.GREEN.name());

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        harness.passUntil(TurnStep.END_OF_COMBAT);

        assertThat(jareth.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Jareth, Leonine Titan");
        harness.assertInGraveyard(player2, "Elvish Warrior");
        harness.assertLife(player1, 20);
    }
}
