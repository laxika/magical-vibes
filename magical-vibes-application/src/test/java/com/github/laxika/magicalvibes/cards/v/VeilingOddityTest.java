package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.CitanulWoodreaders;
import com.github.laxika.magicalvibes.cards.t.Timecrafting;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VeilingOddity.class, CitanulWoodreaders.class, Timecrafting.class})
class VeilingOddityTest extends BaseCardTest {

    @Test
    void suspendExilesWithFourTimeCounters() {
        VeilingOddity card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    void nonLastTimeCounterDoesNotMakeCreaturesUnblockable() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CitanulWoodreaders());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CitanulWoodreaders());
        suspendCard();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsValue(3);
        assertThat(gqs.hasCantBeBlocked(gd, ownCreature)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, opposingCreature)).isFalse();
    }

    @Test
    void lastTimeCounterMakesAllCreaturesUnblockableUntilEndOfTurn() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CitanulWoodreaders());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CitanulWoodreaders());
        suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, ownCreature)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, opposingCreature)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, ownCreature)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, opposingCreature)).isFalse();
    }

    @Test
    void creaturesEnteringLaterAlsoCannotBeBlockedThisTurn() {
        suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        Permanent laterCreature = harness.enterBattlefieldAndReturn(player1, new CitanulWoodreaders());

        assertThat(gqs.hasCantBeBlocked(gd, laterCreature)).isTrue();
    }

    @Test
    void lastCounterTriggerStillResolvesIfTimeCounterIsAddedBeforeItResolves() {
        VeilingOddity card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        gd.exiledCardTimeCounters.put(card.getId(), 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new CitanulWoodreaders());
        assertThat(gqs.hasCantBeBlocked(gd, ownCreature)).isTrue();
    }

    @Test
    void opponentUpkeepDoesNotRemoveTimeCounters() {
        VeilingOddity card = suspendCard();

        advanceToUpkeep(player2);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void lastCounterCanCastOddityForFreeWithHasteAndMakeItUnblockable() {
        VeilingOddity card = suspendCard();

        for (int i = 0; i < 4; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        Permanent oddity = findPermanent(player1, "Veiling Oddity");
        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gqs.hasKeyword(gd, oddity, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, oddity)).isTrue();
    }

    @Test
    void removingAllCountersWithTimecraftingTriggersUnblockabilityOutsideUpkeep() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new CitanulWoodreaders());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new CitanulWoodreaders());
        VeilingOddity card = suspendCard();
        harness.setHand(player1, List.of(new Timecrafting()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castModalInstantForX(player1, 0, 0, 4, card.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gqs.hasCantBeBlocked(gd, ownCreature)).isTrue();
        assertThat(gqs.hasCantBeBlocked(gd, opposingCreature)).isTrue();
    }

    @Test
    void castingNormallyDoesNotMakeCreaturesUnblockable() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new CitanulWoodreaders());
        harness.setHand(player1, List.of(new VeilingOddity()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.hasCantBeBlocked(gd, creature)).isFalse();
        assertThat(gqs.hasCantBeBlocked(gd, findPermanent(player1, "Veiling Oddity"))).isFalse();
    }

    private VeilingOddity suspendCard() {
        VeilingOddity card = new VeilingOddity();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
