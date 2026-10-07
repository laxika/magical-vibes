package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.c.ConundrumSphinx;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UneshCriosphinxSovereign.class, ConundrumSphinx.class, GrizzlyBears.class,
        Island.class, Forest.class, Swamp.class, Plains.class})
class UneshCriosphinxSovereignTest extends BaseCardTest {

    private void addUneshMana() {
        // Unesh costs {4}{U}{U}
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }

    private void castUneshAndReachSeparation(Card... library) {
        harness.setLibrary(player1, List.of(library));
        harness.setHand(player1, List.of(new UneshCriosphinxSovereign()));
        addUneshMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Unesh -> enters, reveal trigger on stack
        harness.passBothPriorities(); // resolve reveal trigger -> opponent separates
    }

    @Test
    @DisplayName("Own ETB reveals four and an opponent is prompted to separate them")
    void ownEnterRevealsFourAndPromptsOpponent() {
        castUneshAndReachSeparation(new Island(), new Forest(), new Swamp(), new Plains());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isTrue();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).hasSize(4);
    }

    @Test
    @DisplayName("Choosing Pile 1 puts it into hand and the other pile into the graveyard")
    void chosenPileToHandOtherToGraveyard() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        castUneshAndReachSeparation(island, forest, swamp, plains);

        // Opponent: Pile 1 = island + forest, Pile 2 = swamp + plains
        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), forest.getId()));

        // Controller chooses Pile 1
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).contains(island, forest);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(swamp, plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(swamp, plains);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("Declining takes the other pile to hand and puts Pile 1 into the graveyard")
    void decliningTakesPileTwoToHand() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        castUneshAndReachSeparation(island, forest, swamp, plains);

        // Opponent: Pile 1 = island + forest, Pile 2 = swamp + plains
        harness.handleMultipleCardsChosen(player2, List.of(island.getId(), forest.getId()));

        // Controller declines -> takes Pile 2
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(swamp, plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(island, forest);
    }

    @Test
    @DisplayName("Another Sphinx you control entering also triggers the reveal")
    void anotherSphinxEnteringTriggersReveal() {
        harness.addToBattlefield(player1, new UneshCriosphinxSovereign());
        harness.setLibrary(player1, List.of(new Island(), new Forest(), new Swamp(), new Plains()));
        harness.setHand(player1, List.of(new ConundrumSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Conundrum Sphinx -> enters, Unesh's ally reveal trigger
        harness.passBothPriorities(); // resolve reveal trigger -> opponent separates

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validCardIds()).hasSize(4);
    }

    @Test
    @DisplayName("A non-Sphinx creature entering does not trigger the reveal")
    void nonSphinxEnteringDoesNotTriggerReveal() {
        harness.addToBattlefield(player1, new UneshCriosphinxSovereign());
        harness.setLibrary(player1, List.of(new Island(), new Forest(), new Swamp(), new Plains()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Grizzly Bears -> enters (no reveal)

        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Sphinx spells you cast cost {2} less")
    void sphinxSpellsCostTwoLess() {
        harness.addToBattlefield(player1, new UneshCriosphinxSovereign());
        // Conundrum Sphinx costs {2}{U}{U}; with the {2} reduction it costs {U}{U}
        harness.setHand(player1, List.of(new ConundrumSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Conundrum Sphinx");
    }

    @Test
    @DisplayName("A Sphinx spell still cannot be cast without enough mana for the reduced cost")
    void cannotCastSphinxWithoutEnoughMana() {
        harness.addToBattlefield(player1, new UneshCriosphinxSovereign());
        harness.setHand(player1, List.of(new ConundrumSphinx()));
        // Only {U} — one short of the reduced {U}{U}
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Own entry creates exactly one reveal trigger")
    void ownEntryCreatesOnlyOneTrigger() {
        harness.setLibrary(player1, List.of(new Island(), new Forest(), new Swamp(), new Plains()));
        harness.setHand(player1, List.of(new UneshCriosphinxSovereign()));
        addUneshMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Unesh does not reduce its own cost while in hand")
    void cannotReduceItsOwnCastingCost() {
        harness.setHand(player1, List.of(new UneshCriosphinxSovereign()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's Unesh does not reduce your Sphinx spells")
    void opposingUneshDoesNotReduceCastingCost() {
        harness.addToBattlefield(player2, new UneshCriosphinxSovereign());
        harness.setHand(player1, List.of(new ConundrumSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Non-Sphinx spells receive no cost reduction")
    void nonSphinxSpellCostsFullMana() {
        harness.addToBattlefield(player1, new UneshCriosphinxSovereign());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opposing Sphinx entering does not trigger your Unesh")
    void opposingSphinxDoesNotTriggerUnesh() {
        harness.addToBattlefield(player2, new UneshCriosphinxSovereign());
        harness.setLibrary(player2, List.of(new Island(), new Forest()));
        harness.setHand(player1, List.of(new ConundrumSphinx()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("A short library reveals only the available cards")
    void shortLibraryRevealsAllAvailableCards() {
        Card island = new Island();
        Card forest = new Forest();
        castUneshAndReachSeparation(island, forest);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(island.getId(), forest.getId());
        harness.handleMultipleCardsChosen(player2, List.of(island.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(island);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing an empty pile puts every revealed card into the graveyard")
    void canChooseEmptyPile() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        castUneshAndReachSeparation(island, forest, swamp, plains);

        harness.handleMultipleCardsChosen(player2, List.of());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(island, forest, swamp, plains);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("Declining an empty pile puts every revealed card into hand")
    void canChooseAllCardsOverEmptyPile() {
        Card island = new Island();
        Card forest = new Forest();
        Card swamp = new Swamp();
        Card plains = new Plains();
        Card fifthCard = new Island();
        castUneshAndReachSeparation(island, forest, swamp, plains, fifthCard);

        harness.handleMultipleCardsChosen(player2, List.of());
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactlyInAnyOrder(island, forest, swamp, plains);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifthCard);
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
    }

    @Test
    @DisplayName("An empty library finishes the trigger without taking any cards")
    void emptyLibraryFinishesTrigger() {
        castUneshAndReachSeparation();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.hasPendingInteraction(PendingPileSeparation.class)).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }
}
