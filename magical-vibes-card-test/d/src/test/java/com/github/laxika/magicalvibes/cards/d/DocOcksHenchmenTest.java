package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.layer.FloatingContinuousEffect;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DocOcksHenchmen.class, Mountain.class})
class DocOcksHenchmenTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking draws, discards a nonland card, and puts a +1/+1 counter on it")
    void attackingWithNonlandDiscardAddsCounter() {
        Permanent henchmen = addReadyHenchmen();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new DocOcksHenchmen()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Doc Ock's Henchmen");

        assertThat(henchmen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    @DisplayName("Attacking with a land discarded does not put a +1/+1 counter on it")
    void attackingWithLandDiscardDoesNotAddCounter() {
        Permanent henchmen = addReadyHenchmen();
        harness.setHand(player1, List.of(new DocOcksHenchmen()));
        harness.setLibrary(player1, List.of(new Mountain()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        discardByName("Mountain");

        assertThat(henchmen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Doc Ock's Henchmen");
    }

    @Test
    @DisplayName("Flash allows casting during the opponent's end step without conniving on entry")
    void castsDuringOpponentsEndStep() {
        gd.playerAutoStopSteps.put(player1.getId(), java.util.Set.of(TurnStep.UPKEEP));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setLibrary(player1, List.of(new Mountain()));

        harness.castFromHand(player1, new DocOcksHenchmen(), "{2}{U}");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Doc Ock's Henchmen");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A nonattacking Henchmen does not connive or receive the attacker's counter")
    void onlyAttackingHenchmenConnives() {
        Permanent attacker = addReadyHenchmen();
        Permanent nonattacker = addReadyHenchmen();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new DocOcksHenchmen(), new Mountain()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        discardByName("Doc Ock's Henchmen");

        assertThat(attacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonattacker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Doc Ock's Henchmen");
    }

    @Test
    @DisplayName("The attack trigger still draws and discards if Henchmen has left the battlefield")
    void connivesAfterLeavingBattlefield() {
        Permanent henchmen = addReadyHenchmen();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new DocOcksHenchmen()));
        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(henchmen);
        gd.playerGraveyards.get(player1.getId()).add(henchmen.getCard());
        harness.passBothPriorities();
        discardByName("Doc Ock's Henchmen");

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(henchmen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Connive uses the creature's current controller after control changes with its trigger on the stack")
    void currentControllerDrawsAndDiscards() {
        Permanent henchmen = addReadyHenchmen();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setHand(player2, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setLibrary(player2, List.of(new DocOcksHenchmen()));
        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).remove(henchmen);
        gd.playerBattlefields.get(player2.getId()).add(henchmen);
        gd.stolenCreatures.put(henchmen.getId(), player1.getId());
        gd.addFloatingEffect(new FloatingContinuousEffect(UUID.randomUUID(), "Control change", null,
                player2.getId(), new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                henchmen.getId(), null, null, EffectDuration.PERMANENT, 0));
        henchmen.setAttacking(false);
        henchmen.setSummoningSick(true);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Mountain", "Doc Ock's Henchmen");
        harness.handleCardChosen(player2, 1);

        assertThat(henchmen.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Doc Ock's Henchmen");
    }

    private Permanent addReadyHenchmen() {
        return addCreatureReady(player1, new DocOcksHenchmen());
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }
}
