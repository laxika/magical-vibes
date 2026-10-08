package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.d.DireWolfProwler;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.n.NeverwinterDryad;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YouMeetInATavern.class, DireWolfProwler.class, NeverwinterDryad.class, Forest.class,
        YouSeeAGuardApproach.class, Island.class})
class YouMeetInATavernTest extends BaseCardTest {

    @Test
    void formAPartyPutsAnyNumberOfRevealedCreaturesIntoHand() {
        Card firstCreature = new DireWolfProwler();
        Card nonCreature = new YouSeeAGuardApproach();
        Card secondCreature = new NeverwinterDryad();
        Card secondNonCreature = new Forest();
        Card thirdNonCreature = new YouSeeAGuardApproach();
        Card cardAfterTopFive = new Island();
        harness.setLibrary(player1, List.of(firstCreature, nonCreature, secondCreature,
                secondNonCreature, thirdNonCreature, cardAfterTopFive));
        prepareSpell();

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(firstCreature, secondCreature);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                cardAfterTopFive, nonCreature, secondNonCreature, thirdNonCreature);
    }

    @Test
    void startABrawlBoostsOnlyYourCreaturesUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new DireWolfProwler());
        prepareSpell();

        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    void formAPartyMayChooseNoCreatures() {
        Card creature = new DireWolfProwler();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(creature, land));
        prepareSpell();

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(creature, land);
    }

    @Test
    void formAPartyCanChooseOnlySomeCreaturesFromAShortLibrary() {
        Card chosen = new DireWolfProwler();
        Card declined = new NeverwinterDryad();
        Card land = new Forest();
        harness.setLibrary(player1, List.of(chosen, declined, land));
        prepareSpell();

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(declined, land);
    }

    @Test
    void formAPartyWithNoCreaturesLeavesTheUnseenCardOnTop() {
        Card unseen = new NeverwinterDryad();
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Island();
        Card fourth = new Island();
        Card fifth = new YouSeeAGuardApproach();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, unseen));
        prepareSpell();

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unseen);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                first, second, third, fourth, fifth, unseen);
    }

    @Test
    void startABrawlDoesNotBoostCreaturesEnteringAfterResolution() {
        prepareSpell();
        harness.castModalSorcery(player1, 0, 1, List.of());
        harness.passBothPriorities();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new DireWolfProwler());

        assertThat(gqs.getEffectivePower(gd, laterCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, laterCreature)).isEqualTo(2);
    }

    @Test
    void formAPartyWithAnEmptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        prepareSpell();

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private void prepareSpell() {
        harness.setHand(player1, List.of(new YouMeetInATavern()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
