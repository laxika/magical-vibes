package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DalekDrone;
import com.github.laxika.magicalvibes.cards.j.JudoonEnforcers;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheMasterFormedAnew.class, JudoonEnforcers.class, DalekDrone.class})
class TheMasterFormedAnewTest extends BaseCardTest {

    @Test
    void bodyThiefExilesAndMarksAControlledCreature() {
        Card creature = new JudoonEnforcers();
        addCreatureReady(player1, creature);
        harness.castFromHand(player1, new TheMasterFormedAnew(), "{U}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(creature.getId());
        assertThat(gd.exiledCardsWithTakeoverCounters).contains(creature.getId());
    }

    @Test
    void entersAsACopyOfATakeoverMarkedCreatureInExile() {
        Card exiledCreature = new JudoonEnforcers();
        harness.setExile(player1, List.of(exiledCreature));
        gd.exiledCardsWithTakeoverCounters.add(exiledCreature.getId());
        harness.castFromHand(player1, new TheMasterFormedAnew(), "{U}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, exiledCreature.getName());
    }

    @Test
    void decliningBodyThiefLeavesTheCreatureOnTheBattlefield() {
        Card creature = new JudoonEnforcers();
        addCreatureReady(player1, creature);
        harness.castFromHand(player1, new TheMasterFormedAnew(), "{U}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, creature.getName());
        harness.assertOnBattlefield(player1, "The Master, Formed Anew");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exiledCardsWithTakeoverCounters).isEmpty();
    }

    @Test
    void bodyThiefChoosesOneControlledCreatureAndCopiesIt() {
        Card chosen = new JudoonEnforcers();
        Card other = new JudoonEnforcers();
        var chosenPermanent = addCreatureReady(player1, chosen);
        var otherPermanent = addCreatureReady(player1, other);
        var opposingPermanent = addCreatureReady(player2, new JudoonEnforcers());
        harness.castFromHand(player1, new TheMasterFormedAnew(), "{U}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, chosenPermanent.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(otherPermanent).doesNotContain(chosenPermanent);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(opposingPermanent);
        assertThat(gd.exiledCardsWithTakeoverCounters).contains(chosen.getId()).doesNotContain(other.getId());

        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(countPermanents(player1, chosen.getName())).isEqualTo(2);
        harness.assertNotOnBattlefield(player1, "The Master, Formed Anew");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(chosen);
    }

    @Test
    void canDeclineCopyingAMarkedCreature() {
        Card marked = new JudoonEnforcers();
        harness.setExile(player1, List.of(marked));
        gd.exiledCardsWithTakeoverCounters.add(marked.getId());
        harness.castFromHand(player1, new TheMasterFormedAnew(), "{U}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "The Master, Formed Anew");
        harness.assertNotOnBattlefield(player1, marked.getName());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(marked);
    }

    @Test
    void choosesAmongMarkedCardsIncludingAnOpponentsCard() {
        Card ownMarked = new TheMasterFormedAnew();
        Card opponentMarked = new JudoonEnforcers();
        Card unmarked = new JudoonEnforcers();
        harness.setExile(player1, List.of(ownMarked, unmarked));
        harness.setExile(player2, List.of(opponentMarked));
        gd.exiledCardsWithTakeoverCounters.add(ownMarked.getId());
        gd.exiledCardsWithTakeoverCounters.add(opponentMarked.getId());
        harness.castFromHand(player1, new TheMasterFormedAnew(), "{U}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        var choice = (PendingInteraction.ExiledCreatureCopyChoice) gd.interaction.activeInteraction();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(ownMarked.getId(), opponentMarked.getId());
        harness.handleMultipleCardsChosen(player1, List.of(opponentMarked.getId()));

        harness.assertOnBattlefield(player1, opponentMarked.getName());
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(opponentMarked);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(ownMarked, unmarked);
    }

    @Test
    void bodyThiefMarksThePhysicalCardWhenExilingACopy() {
        Card marked = new JudoonEnforcers();
        Card firstMaster = new TheMasterFormedAnew();
        harness.setExile(player1, List.of(marked));
        gd.exiledCardsWithTakeoverCounters.add(marked.getId());
        harness.castFromHand(player1, firstMaster, "{U}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.assertOnBattlefield(player1, marked.getName());

        harness.castFromHand(player1, new TheMasterFormedAnew(), "{U}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(firstMaster);
        assertThat(gd.exiledCardsWithTakeoverCounters).contains(firstMaster.getId());
    }

    @Test
    void acceptingBodyThiefWithNoControlledCreatureDoesNothing() {
        harness.castFromHand(player1, new TheMasterFormedAnew(), "{U}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "The Master, Formed Anew");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.exiledCardsWithTakeoverCounters).isEmpty();
    }

    @Test
    void copiedCreaturesEnterAbilityTriggers() {
        Card marked = new DalekDrone();
        harness.setExile(player1, List.of(marked));
        gd.exiledCardsWithTakeoverCounters.add(marked.getId());
        var target = addCreatureReady(player2, new JudoonEnforcers());
        harness.setLife(player2, 20);

        harness.castFromHand(player1, new TheMasterFormedAnew(), "{U}{B}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Dalek Drone");
        harness.assertInGraveyard(player2, "Judoon Enforcers");
        harness.assertLife(player2, 17);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(marked);
    }
}
