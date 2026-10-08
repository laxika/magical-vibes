package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.k.KozileksChanneler;
import com.github.laxika.magicalvibes.cards.p.PlatedCrusher;
import com.github.laxika.magicalvibes.cards.r.RuinProcessor;
import com.github.laxika.magicalvibes.cards.s.SilentSkimmer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({
        ConduitOfRuin.class,
        RuinProcessor.class,
        PlatedCrusher.class,
        KozileksChanneler.class,
        SilentSkimmer.class
})
class ConduitOfRuinTest extends BaseCardTest {

    @Test
    @DisplayName("The cast trigger may put an eligible colorless creature on top of the library")
    void castTriggerSearchesEligibleCreature() {
        Card eligible = new RuinProcessor();
        Card colored = new PlatedCrusher();
        Card tooSmall = new KozileksChanneler();
        castConduit(eligible, colored, tooSmall);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(eligible);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(eligible);
    }

    @Test
    @DisplayName("Declining the cast trigger does not search")
    void decliningCastTriggerDoesNotSearch() {
        Card eligible = new RuinProcessor();
        castConduit(eligible);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligible);
    }

    @Test
    @DisplayName("Only the first creature spell each turn gets the cost reduction")
    void onlyFirstCreatureSpellEachTurnIsReduced() {
        addCreatureReady(player1, new ConduitOfRuin());
        harness.setHand(player1, List.of(new KozileksChanneler(), new KozileksChanneler()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castConduit(Card... library) {
        harness.setHand(player1, List.of(new ConduitOfRuin()));
        harness.setLibrary(player1, List.of(library));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("A restricted search may fail to find even when an eligible card exists")
    void searchMayFailToFind() {
        Card eligible = new RuinProcessor();
        castConduit(eligible);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(eligible);
    }

    @Test
    @DisplayName("Searching with no eligible cards finishes without moving a card")
    void searchWithNoEligibleCards() {
        Card colored = new PlatedCrusher();
        Card tooSmall = new KozileksChanneler();
        castConduit(colored, tooSmall);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(colored, tooSmall);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting Conduit itself uses the first creature spell for that turn")
    void castingConduitAlreadyUsesFirstCreatureSpell() {
        castConduit(new KozileksChanneler());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Conduit of Ruin");
        harness.setHand(player1, List.of(new KozileksChanneler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Multiple Conduits reduce the same first creature spell")
    void multipleConduitsStackReductions() {
        addCreatureReady(player1, new ConduitOfRuin());
        addCreatureReady(player1, new ConduitOfRuin());
        harness.setHand(player1, List.of(new KozileksChanneler()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Kozilek's Channeler");
    }

    @Test
    @DisplayName("The reduction preserves colored mana requirements on a devoid spell")
    void reductionDoesNotPayColoredMana() {
        addCreatureReady(player1, new ConduitOfRuin());
        harness.setHand(player1, List.of(new SilentSkimmer()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Silent Skimmer");
    }

    @Test
    @DisplayName("An opponent's Conduit does not reduce your creature spells")
    void opponentsConduitDoesNotReduceCost() {
        addCreatureReady(player2, new ConduitOfRuin());
        harness.setHand(player1, List.of(new KozileksChanneler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The first creature discount becomes available again on the next turn")
    void reductionResetsOnLaterTurn() {
        harness.setHand(player2, List.of());
        addCreatureReady(player1, new ConduitOfRuin());
        harness.setHand(player1, List.of(new KozileksChanneler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new KozileksChanneler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Kozilek's Channeler")).isEqualTo(2);
    }
}
