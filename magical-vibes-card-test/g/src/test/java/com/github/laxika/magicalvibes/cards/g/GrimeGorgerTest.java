package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
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

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrimeGorger.class, GrizzlyBears.class, Shock.class, Ornithopter.class, GroundSeal.class})
class GrimeGorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking exiles one card of each chosen card type and grows Grime Gorger")
    void exilesOneOfEachChosenCardType() {
        Card creature = new GrizzlyBears();
        Card instant = new Shock();
        Card otherCreature = new GrizzlyBears();
        Card ownInstant = new Shock();
        harness.setGraveyard(player2, List.of(creature, instant, otherCreature));
        harness.setGraveyard(player1, List.of(ownInstant));

        Permanent gorger = addCreatureReady(player1, new GrimeGorger());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(gorger)));
        resolveAllTriggers();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                creature.getId(), instant.getId(), otherCreature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), instant.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(creature, instant);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(otherCreature);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownInstant);
        assertThat(gorger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Two cards with the same card type cannot both be chosen")
    void rejectsDuplicateCardTypes() {
        Card firstCreature = new GrizzlyBears();
        Card secondCreature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(firstCreature, secondCreature));

        Permanent gorger = addCreatureReady(player1, new GrimeGorger());
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(gorger)));
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(firstCreature.getId(), secondCreature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Selected cards must have different card types");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
    }

    @Test
    @DisplayName("Graveyard cards are chosen during resolution, after the response window")
    void choosesCardsOnlyWhenAttackTriggerResolves() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        addCreatureReady(player1, new GrimeGorger());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveAllTriggers();
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
    }

    @Test
    @DisplayName("An artifact creature and a creature can occupy different card type slots")
    void assignsMultitypeCardsToSeparateSlots() {
        Card artifactCreature = new Ornithopter();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(artifactCreature, creature));
        Permanent gorger = addCreatureReady(player1, new GrimeGorger());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of(artifactCreature.getId(), creature.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(artifactCreature, creature);
        assertThat(gorger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("A single artifact creature gives only one counter")
    void countsExiledCardsRatherThanTheirTypes() {
        Card artifactCreature = new Ornithopter();
        harness.setGraveyard(player2, List.of(artifactCreature));
        Permanent gorger = addCreatureReady(player1, new GrimeGorger());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of(artifactCreature.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(artifactCreature);
        assertThat(gorger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller may choose no cards from a nonempty graveyard")
    void mayExileNoCards() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        Permanent gorger = addCreatureReady(player1, new GrimeGorger());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gorger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An empty defending graveyard causes no exiles or counters")
    void emptyGraveyardDoesNotGrowGorger() {
        harness.setGraveyard(player2, List.of());
        Permanent gorger = addCreatureReady(player1, new GrimeGorger());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gorger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
    }

    @Test
    @DisplayName("Ground Seal does not stop the nontargeting graveyard choice")
    void exilesCardsDespiteGroundSeal() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addToBattlefield(player2, new GroundSeal());
        Permanent gorger = addCreatureReady(player1, new GrimeGorger());
        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(gorger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
