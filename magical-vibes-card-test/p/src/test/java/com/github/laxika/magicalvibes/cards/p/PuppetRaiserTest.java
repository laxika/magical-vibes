package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.CentaurCourser;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LurkerInTheDeep;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PuppetRaiser.class, GrizzlyBears.class, CentaurCourser.class,
        HillGiant.class, Forest.class, LurkerInTheDeep.class})
class PuppetRaiserTest extends BaseCardTest {

    @Test
    void exilesCreatureAndSeeksOneManaValueHigherWithPerpetualMenace() {
        harness.addToBattlefield(player1, new PuppetRaiser());
        Card exiledCreature = new GrizzlyBears();
        Card soughtCreature = new CentaurCourser();
        Card wrongManaValue = new HillGiant();
        Card nonCreature = new Forest();
        harness.setGraveyard(player1, List.of(exiledCreature));
        harness.setLibrary(player1, List.of(wrongManaValue, soughtCreature, nonCreature));

        advanceToEndStep(player1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(exiledCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(exiledCreature);
        assertThat(gd.playerHands.get(player1.getId())).contains(soughtCreature);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(wrongManaValue, nonCreature);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, soughtCreature, "{2}{G}");
        harness.passBothPriorities();

        Permanent soughtPermanent = findPermanent(player1, "Centaur Courser");
        assertThat(gqs.hasKeyword(gd, soughtPermanent, Keyword.MENACE)).isTrue();
    }

    @Test
    void upToOneAllowsDecliningTheGraveyardTarget() {
        harness.addToBattlefield(player1, new PuppetRaiser());
        Card creature = new GrizzlyBears();
        Card libraryCreature = new CentaurCourser();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(libraryCreature));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCreature);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(libraryCreature);
    }

    @Test
    void onlyUsesCreatureCardsFromYourGraveyard() {
        harness.addToBattlefield(player1, new PuppetRaiser());
        Card nonCreature = new Forest();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(nonCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(nonCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
    }

    @Test
    void stillExilesWhenNoCreatureHasTheRequiredManaValue() {
        harness.addToBattlefield(player1, new PuppetRaiser());
        Card creature = new GrizzlyBears();
        Card wrongManaValue = new HillGiant();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(wrongManaValue));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(wrongManaValue);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(wrongManaValue);
    }

    @Test
    void doesNotSeekWhenTargetLeavesTheGraveyardBeforeResolution() {
        harness.addToBattlefield(player1, new PuppetRaiser());
        Card creature = new GrizzlyBears();
        Card sought = new CentaurCourser();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(sought));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(sought);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(sought);
    }

    @Test
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new PuppetRaiser());
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));

        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void seekingTriggersLurkerToManifestADuplicate() {
        harness.addToBattlefield(player1, new PuppetRaiser());
        harness.addToBattlefield(player1, new LurkerInTheDeep());
        Card creature = new GrizzlyBears();
        Card sought = new CentaurCourser();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLibrary(player1, List.of(sought));

        advanceToEndStep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).contains(sought);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isFaceDown).hasSize(1);
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
