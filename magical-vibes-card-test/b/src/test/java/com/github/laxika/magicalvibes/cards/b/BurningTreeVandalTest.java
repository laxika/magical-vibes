package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.r.RhythmOfTheWild;
import com.github.laxika.magicalvibes.cards.s.Solemnity;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({BurningTreeVandal.class, RhythmOfTheWild.class, Solemnity.class})
class BurningTreeVandalTest extends BaseCardTest {

    @Test
    @DisplayName("Choosing the Riot counter gives Burning-Tree Vandal a +1/+1 counter")
    void riotAddsCounter() {
        Permanent vandal = castVandal(true);

        assertThat(vandal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, vandal)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, vandal)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, vandal, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Choosing Riot haste gives it haste beyond the turn it entered")
    void riotAddsPersistentHaste() {
        Permanent vandal = castVandal(false);

        assertThat(gqs.hasKeyword(gd, vandal, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, vandal, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Riot automatically gives haste when a +1/+1 counter cannot be placed")
    void riotGivesHasteWhenCountersAreForbidden() {
        harness.addToBattlefield(player1, new Solemnity());

        Permanent vandal = castVandal(false);

        assertThat(vandal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, vandal, Keyword.HASTE)).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Attacking lets Burning-Tree Vandal discard a card to draw a card")
    void attackTriggerDiscardsThenDraws() {
        addCreatureReady(player1, new BurningTreeVandal());
        BurningTreeVandal discarded = new BurningTreeVandal();
        RhythmOfTheWild drawn = new RhythmOfTheWild();
        harness.setHand(player1, List.of(discarded));
        harness.setLibrary(player1, List.of(drawn));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    @DisplayName("Declining Burning-Tree Vandal's attack trigger does not discard or draw")
    void decliningAttackTriggerDoesNothing() {
        addCreatureReady(player1, new BurningTreeVandal());
        BurningTreeVandal retained = new BurningTreeVandal();
        harness.setHand(player1, List.of(retained));
        harness.setLibrary(player1, List.of(new RhythmOfTheWild()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Accepting the attack trigger with an empty hand does not draw")
    void emptyHandDoesNotDraw() {
        addCreatureReady(player1, new BurningTreeVandal());
        RhythmOfTheWild undrawn = new RhythmOfTheWild();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(undrawn));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
            harness.handleMayAbilityChosen(player1, true);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(undrawn);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The attacking creature's controller discards and draws")
    void attackTriggerUsesAttackingController() {
        addCreatureReady(player2, new BurningTreeVandal());
        BurningTreeVandal discarded = new BurningTreeVandal();
        RhythmOfTheWild drawn = new RhythmOfTheWild();
        RhythmOfTheWild retained = new RhythmOfTheWild();
        harness.setHand(player2, List.of(discarded));
        harness.setLibrary(player2, List.of(drawn));
        harness.setHand(player1, List.of(retained));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(retained);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Native Riot and Riot from Rhythm of the Wild can each add a counter")
    void multipleRiotInstancesCanAddTwoCounters() {
        harness.addToBattlefield(player1, new RhythmOfTheWild());

        Permanent vandal = castVandal(true, true);

        assertThat(vandal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, vandal, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Native Riot and Riot from Rhythm of the Wild can give a counter and haste")
    void multipleRiotInstancesCanGiveCounterAndHaste() {
        harness.addToBattlefield(player1, new RhythmOfTheWild());

        Permanent vandal = castVandal(true, false);

        assertThat(vandal.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, vandal, Keyword.HASTE)).isTrue();
    }

    private Permanent castVandal(boolean... counterChoices) {
        harness.setHand(player1, List.of(new BurningTreeVandal()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        for (boolean chooseCounter : counterChoices) {
            if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
                harness.handleMayAbilityChosen(player1, chooseCounter);
            }
        }
        return findPermanent(player1, "Burning-Tree Vandal");
    }
}
