package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BaldinCenturyHerdmaster.class, FountainOfYouth.class, GrizzlyBears.class})
class BaldinCenturyHerdmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking lets you boost up to one hundred target creatures by your hand size")
    void attackBoostsSelectedCreaturesByHandSize() {
        Permanent baldin = addReadyPermanent(player1, new BaldinCenturyHerdmaster());
        Permanent ownTarget = addReadyStats(player1, 1, 1);
        Permanent opposingTarget = addReadyStats(player2, 1, 1);
        Permanent artifact = addReadyPermanent(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        beginCombat(player1);
        gs.declareAttackers(gd, player1, List.of(indexOf(player1, baldin)));

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice.validIds()).contains(ownTarget.getId(), opposingTarget.getId())
                .doesNotContain(artifact.getId());
        harness.handleMultiplePermanentsChosen(player1, List.of(ownTarget.getId(), opposingTarget.getId()));
        harness.passBothPriorities();

        assertThat(ownTarget.getPowerModifier()).isEqualTo(0);
        assertThat(ownTarget.getToughnessModifier()).isEqualTo(3);
        assertThat(opposingTarget.getToughnessModifier()).isEqualTo(3);

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.UNTAP);

        assertThat(ownTarget.getToughnessModifier()).isEqualTo(0);
        assertThat(opposingTarget.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("During your turn every creature assigns combat damage equal to its toughness")
    void usesToughnessForAllCreaturesDuringYourTurn() {
        addReadyPermanent(player1, new BaldinCenturyHerdmaster());
        Permanent attacker = addReadyStats(player1, 1, 5);
        Permanent blocker = addReadyStats(player2, 1, 4);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(indexOf(player1, attacker));

        beginCombatDamage(player1);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Baldin's combat-damage replacement is inactive during another player's turn")
    void doesNotUseToughnessDuringOpponentsTurn() {
        addReadyPermanent(player1, new BaldinCenturyHerdmaster());
        Permanent attacker = addReadyStats(player2, 1, 4);
        Permanent blocker = addReadyStats(player1, 1, 5);
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(indexOf(player2, attacker));

        beginCombatDamage(player2);
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(1);
    }

    private Permanent addReadyPermanent(Player player, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private Permanent addReadyStats(Player player, int power, int toughness) {
        GrizzlyBears card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        return addReadyPermanent(player, card);
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    private void beginCombat(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private void beginCombatDamage(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }
}
