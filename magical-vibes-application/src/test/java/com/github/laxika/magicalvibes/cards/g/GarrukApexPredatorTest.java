package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.l.LilianaVess;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.p.PlatinumEmperion;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GarrukApexPredator.class, LilianaVess.class, RuneclawBear.class, PlatinumEmperion.class})
class GarrukApexPredatorTest extends BaseCardTest {

    @Test
    @DisplayName("+1 destroys another planeswalker")
    void plusOneDestroysPlaneswalker() {
        Permanent garruk = addReadyGarruk(player1, 5);
        harness.addToBattlefield(player2, new LilianaVess());
        Permanent liliana = findPermanent(player2, "Liliana Vess");
        liliana.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, 0, null, liliana.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Liliana Vess");
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("+1 cannot target Garruk himself")
    void plusOneCannotTargetSelf() {
        Permanent garruk = addReadyGarruk(player1, 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, garruk.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("+1 cannot target a creature")
    void plusOneCannotTargetCreature() {
        addReadyGarruk(player1, 5);
        harness.addToBattlefield(player2, new RuneclawBear());
        Permanent bears = findPermanent(player2, "Runeclaw Bear");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("+1 creates a 3/3 Beast token with deathtouch")
    void plusOneCreatesBeastToken() {
        Permanent garruk = addReadyGarruk(player1, 5);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(3);
        assertThat(token.getCard().getKeywords()).contains(Keyword.DEATHTOUCH);
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
    }

    @Test
    @DisplayName("-3 destroys a creature and gains life equal to its toughness")
    void minusThreeDestroysAndGainsLife() {
        Permanent garruk = addReadyGarruk(player1, 5);
        harness.addToBattlefield(player2, new RuneclawBear());
        Permanent bears = findPermanent(player2, "Runeclaw Bear");

        harness.activateAbility(player1, 0, 2, null, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertLife(player1, 22);
        assertThat(garruk.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
    }

    @Test
    @DisplayName("-8 gives the emblem to the target opponent, not to Garruk's controller")
    void minusEightGivesEmblemToOpponent() {
        addReadyGarruk(player1, 9);

        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.emblems).hasSize(1);
        assertThat(gd.emblems.getFirst().controllerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("-8 logs the emblem against the target opponent's name")
    void minusEightLogsEmblemAgainstTargetOpponent() {
        addReadyGarruk(player1, 9);

        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).contains(
                player2.getUsername() + " gets an emblem with \"Whenever a creature attacks you, "
                        + "it gets +5/+5 and gains trample until end of turn.\".");
    }

    @Test
    @DisplayName("Creatures attacking the emblem's controller get +5/+5 and trample")
    void emblemBoostsAttackingCreature() {
        addReadyGarruk(player1, 9);
        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        Permanent bears = addReadyAttacker(player1);
        declareAttack(player1, bears, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(7);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The emblem does not trigger when a creature attacks a planeswalker instead")
    void emblemDoesNotTriggerOnPlaneswalkerAttack() {
        addReadyGarruk(player1, 9);
        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        harness.addToBattlefield(player2, new LilianaVess());
        Permanent liliana = findPermanent(player2, "Liliana Vess");
        liliana.setCounterCount(CounterType.LOYALTY, 5);

        Permanent bears = addReadyAttacker(player1);
        declareAttack(player1, bears, liliana.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The emblem does not boost creatures attacking the player without it")
    void emblemDoesNotBoostAttacksOnOtherPlayer() {
        addReadyGarruk(player1, 9);
        harness.activateAbility(player1, 0, 3, null, player2.getId());
        harness.passBothPriorities();

        Permanent bears = addReadyAttacker(player2);
        declareAttack(player2, bears, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("-3 destroys Platinum Emperion before gaining life")
    void minusThreeGainsLifeAfterRemovingLifeTotalRestriction() {
        addReadyGarruk(player1, 5);
        Permanent emperion = harness.addToBattlefieldAndReturn(player1, new PlatinumEmperion());

        harness.activateAbility(player1, 0, 2, null, emperion.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Platinum Emperion");
        harness.assertLife(player1, 28);
    }

    @Test
    @DisplayName("-8 cannot give Garruk's controller an emblem")
    void minusEightCannotTargetController() {
        addReadyGarruk(player1, 9);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 3, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("-3 uses the creature's effective toughness including counters")
    void minusThreeUsesEffectiveToughness() {
        addReadyGarruk(player1, 5);
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new RuneclawBear());
        bear.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.activateAbility(player1, 0, 2, null, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Runeclaw Bear");
        harness.assertLife(player1, 25);
    }

    private void declareAttack(Player attackingPlayer, Permanent attacker, java.util.UUID defenderId) {
        int index = gd.playerBattlefields.get(attackingPlayer.getId()).indexOf(attacker);
        if (gd.playerIds.contains(defenderId)) {
            declareAttackers(attackingPlayer, List.of(index));
            return;
        }
        harness.forceActivePlayer(attackingPlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, attackingPlayer, List.of(index), Map.of(index, defenderId));
    }

    private Permanent addReadyAttacker(Player player) {
        return addCreatureReady(player, new RuneclawBear());
    }

    private Permanent addReadyGarruk(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new GarrukApexPredator());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }
}
