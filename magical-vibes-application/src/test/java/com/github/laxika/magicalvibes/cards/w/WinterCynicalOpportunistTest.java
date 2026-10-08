package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.Regrowth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.p.Putrefy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WinterCynicalOpportunist.class, Regrowth.class, Forest.class,
        GrizzlyBears.class, Shock.class, Putrefy.class})
class WinterCynicalOpportunistTest extends BaseCardTest {

    @Test
    void attacksAndMillsThreeCards() {
        Card forest = new Forest();
        Card shock = new Shock();
        Card ritual = new Regrowth();
        harness.setLibrary(player1, List.of(forest, shock, ritual));
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

    @Test
    void millsAllRemainingCardsWhenLibraryHasFewerThanThree() {
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        addCreatureReady(player1, new WinterCynicalOpportunist());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void mayDeclineWithoutExilingAnyCards() {
        Card forest = new Forest();
        Card shock = new Shock();
        Card regrowth = new Regrowth();
        Card bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new WinterCynicalOpportunist());
        harness.setGraveyard(player1, List.of(forest, shock, regrowth, bears));

        beginEndStepAbility();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(forest, shock, regrowth, bears);
        assertThat(gd.findExiledCard(forest.getId())).isNull();
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void canReturnLandWithFinalityCounter() {
        Card forest = new Forest();
        Card shock = new Shock();
        Card regrowth = new Regrowth();
        Card bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new WinterCynicalOpportunist());
        harness.setGraveyard(player1, List.of(forest, shock, regrowth, bears));

        beginEndStepAbility();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1,
                List.of(forest.getId(), shock.getId(), regrowth.getId(), bears.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));

        assertThat(findPermanent(player1, "Forest").getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
        assertThat(gd.findExiledCard(forest.getId())).isNull();
    }

    @Test
    void finalityExilesReturnedCreatureInsteadOfPuttingItInGraveyard() {
        Card forest = new Forest();
        Card shock = new Shock();
        Card regrowth = new Regrowth();
        Card bears = new GrizzlyBears();
        harness.addToBattlefield(player1, new WinterCynicalOpportunist());
        harness.setGraveyard(player1, List.of(forest, shock, regrowth, bears));

        beginEndStepAbility();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMultipleCardsChosen(player1,
                List.of(forest.getId(), shock.getId(), regrowth.getId(), bears.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        Permanent returned = findPermanent(player1, "Grizzly Bears");
        harness.setHand(player1, List.of(new Putrefy()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.ensurePriority(player1);
        harness.castAndResolveInstant(player1, 0, returned.getId());

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
    }

    @Test
    void doesNotTriggerAtOpponentsEndStep() {
        harness.addToBattlefield(player1, new WinterCynicalOpportunist());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canExileAndReturnWinterIfItDiesInResponseToItsTrigger() {
        Card winter = new WinterCynicalOpportunist();
        Card forest = new Forest();
        Card regrowth = new Regrowth();
        Permanent source = harness.addToBattlefieldAndReturn(player1, winter);
        harness.setGraveyard(player1, List.of(forest, regrowth));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        harness.setHand(player1, List.of(new Putrefy()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, source.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Winter, Cynical Opportunist");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.cards()).contains(winter);
        Card putrefy = gd.playerGraveyards.get(player1.getId()).stream()
                .filter(card -> card instanceof Putrefy).findFirst().orElseThrow();
        harness.handleMultipleCardsChosen(player1,
                List.of(winter.getId(), forest.getId(), regrowth.getId(), putrefy.getId()));
        harness.handleMultipleCardsChosen(player1, List.of(winter.getId()));

        assertThat(findPermanent(player1, "Winter, Cynical Opportunist")
                .getCounterCount(CounterType.FINALITY)).isEqualTo(1);
        harness.assertNotInGraveyard(player1, "Winter, Cynical Opportunist");
    }

    private void beginEndStepAbility() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

}
