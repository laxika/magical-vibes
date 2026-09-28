package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.Regrowth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WinterCynicalOpportunist.class, Regrowth.class, Forest.class,
        GrizzlyBears.class, Shock.class})
class WinterCynicalOpportunistTest extends BaseCardTest {

    @Test
    void attacksAndMillsThreeCards() {
        Card forest = new Forest();
        Card shock = new Shock();
        Card ritual = new Regrowth();
        setLibrary(forest, shock, ritual);
        addCreatureReady(player1, new WinterCynicalOpportunist());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest, shock, ritual);
    }

    @Test
    void exilesFourCardTypesThenReturnsAChosenPermanentWithFinality() {
        Card forest = new Forest();
        Card shock = new Shock();
        Card ritual = new Regrowth();
        Card bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new WinterCynicalOpportunist());
        harness.setGraveyard(player1, List.of(forest, shock, ritual, bears));

        beginEndStepAbility();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);

        harness.handleMultipleCardsChosen(player1,
                List.of(forest.getId(), shock.getId(), ritual.getId(), bears.getId()));

        PendingInteraction.LibraryRevealChoice permanentChoice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(permanentChoice).isNotNull();
        assertThat(permanentChoice.validCardIds()).containsExactlyInAnyOrder(forest.getId(), bears.getId());
        assertThat(permanentChoice.minCount()).isEqualTo(1);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(returned.getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.findExiledCard(forest.getId())).isNotNull();
        assertThat(gd.findExiledCard(shock.getId())).isNotNull();
        assertThat(gd.findExiledCard(ritual.getId())).isNotNull();
        assertThat(gd.findExiledCard(bears.getId())).isNull();
    }

    @Test
    void rejectsASelectionWithFewerThanFourCardTypes() {
        Card forest = new Forest();
        Card shock = new Shock();
        Card ritual = new Regrowth();
        harness.addToBattlefield(player1, new WinterCynicalOpportunist());
        harness.setGraveyard(player1, List.of(forest, shock, ritual));

        beginEndStepAbility();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1,
                List.of(forest.getId(), shock.getId(), ritual.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest, shock, ritual);
        assertThat(gd.findExiledCard(forest.getId())).isNull();
    }

    private void beginEndStepAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    private void setLibrary(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
