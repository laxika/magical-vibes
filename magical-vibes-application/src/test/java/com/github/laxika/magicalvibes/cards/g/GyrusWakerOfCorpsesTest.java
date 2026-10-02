package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GyrusWakerOfCorpses.class, GrizzlyBears.class, AirElemental.class})
class GyrusWakerOfCorpsesTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with counters equal to the total mana spent to cast it")
    void entersWithCountersEqualToManaSpent() {
        harness.setHand(player1, List.of(new GyrusWakerOfCorpses()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, 2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent gyrus = findPermanent(player1, "Gyrus, Waker of Corpses");
        assertThat(gyrus.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("Only smaller creature cards are offered as attack targets")
    void onlySmallerCreatureCardsCanBeTargeted() {
        Card smallerCreature = new GrizzlyBears();
        Card anotherSmallerCreature = new GrizzlyBears();
        Card largerCreature = new AirElemental();
        harness.setGraveyard(player1, List.of(smallerCreature, anotherSmallerCreature, largerCreature));
        addReadyGyrus(3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.GraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIndices()).containsExactly(0, 1);
        harness.handleGraveyardCardChosen(player1, choice.validIndices().getFirst());
    }

    @Test
    @DisplayName("Accepting the attack trigger creates a tapped attacking copy and exiles it at end of combat")
    void acceptingAttackTriggerCreatesTappedAttackingCopyUntilEndOfCombat() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addReadyGyrus(3);
        gd.playerAutoStopSteps.values().forEach(stops -> stops.add(TurnStep.DECLARE_BLOCKERS));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"))
                .findFirst()
                .orElseThrow();
        assertThat(token.isTapped()).isTrue();
        assertThat(token.isAttacking()).isTrue();
        assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getName)
                .contains("Grizzly Bears");

        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the graveyard unchanged")
    void decliningAttackTriggerDoesNothing() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        addReadyGyrus(3);

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    private Permanent addReadyGyrus(int counterCount) {
        Permanent gyrus = addCreatureReady(player1, new GyrusWakerOfCorpses());
        gyrus.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counterCount);
        return gyrus;
    }
}
