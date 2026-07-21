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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class UneshCriosphinxSovereignTest extends BaseCardTest {

    private void addUneshMana() {
        // Unesh costs {4}{U}{U}
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 2);
    }

    private void castUneshAndReachSeparation(Card island, Card forest, Card swamp, Card plains) {
        harness.setLibrary(player1, List.of(island, forest, swamp, plains));
        harness.setHand(player1, List.of(new UneshCriosphinxSovereign()));
        addUneshMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve Unesh -> enters, reveal trigger on stack
        harness.passBothPriorities(); // resolve reveal trigger -> opponent separates
    }

    // ===== Enters: reveal top four, opponent separates into two piles =====

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

    // ===== Sphinx cost reduction =====

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
}
