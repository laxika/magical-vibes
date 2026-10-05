package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.IronshellBeetle;
import com.github.laxika.magicalvibes.cards.n.NantukoMonastery;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronshellBeetle.class, KrosanWayfarer.class, NantukoMonastery.class})
class KrosanWayfarerTest extends BaseCardTest {

    @Test
    void sacrificingWayfarerIsPaidBeforeAbilityResolves() {
        addCreatureReady(player1, new KrosanWayfarer());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Krosan Wayfarer");
        harness.assertNotOnBattlefield(player1, "Krosan Wayfarer");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void sacrificeAbilityCanBeActivatedWhileSummoningSick() {
        Permanent wayfarer = harness.addToBattlefieldAndReturn(player1, new KrosanWayfarer());
        wayfarer.setSummoningSick(true);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Krosan Wayfarer");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void acceptingMayPutsLandFromHandOntoBattlefieldUntapped() {
        addCreatureReady(player1, new KrosanWayfarer());
        NantukoMonastery land = new NantukoMonastery();
        IronshellBeetle creature = new IronshellBeetle();
        harness.setHand(player1, List.of(land, creature));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.HandCardChoice.class);
        PendingInteraction.HandCardChoice choice =
                (PendingInteraction.HandCardChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(0);
        harness.handleCardChosen(player1, 0);

        Permanent battlefieldLand = findPermanent(player1, "Nantuko Monastery");
        assertThat(battlefieldLand.isTapped()).isFalse();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
    }

    @Test
    void decliningMayLeavesLandInHand() {
        addCreatureReady(player1, new KrosanWayfarer());
        NantukoMonastery land = new NantukoMonastery();
        harness.setHand(player1, List.of(land));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(land);
        harness.assertNotOnBattlefield(player1, "Nantuko Monastery");
    }

    @Test
    void acceptingMayWithNoLandLeavesHandUnchanged() {
        addCreatureReady(player1, new KrosanWayfarer());
        KrosanWayfarer nonlandCard = new KrosanWayfarer();
        harness.setHand(player1, List.of(nonlandCard));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.HandCardChoice.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(nonlandCard);
        harness.assertInGraveyard(player1, "Krosan Wayfarer");
    }

    @Test
    void choosingOneOfMultipleLandsDoesNotUseALandPlay() {
        addCreatureReady(player1, new KrosanWayfarer());
        NantukoMonastery firstLand = new NantukoMonastery();
        NantukoMonastery secondLand = new NantukoMonastery();
        harness.setHand(player1, List.of(firstLand, secondLand));
        gd.landsPlayedThisTurn.put(player1.getId(), 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstLand);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard).containsExactly(secondLand);
        assertThat(gd.landsPlayedThisTurn.get(player1.getId())).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedWayfarerPutsLandFromItsControllersHand() {
        Permanent wayfarer = harness.addToBattlefieldAndReturn(player2, new KrosanWayfarer());
        wayfarer.setTapped(true);
        NantukoMonastery controllerLand = new NantukoMonastery();
        NantukoMonastery opponentLand = new NantukoMonastery();
        harness.setHand(player2, List.of(controllerLand));
        harness.setHand(player1, List.of(opponentLand));
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Krosan Wayfarer");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(opponentLand);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(Permanent::getCard).containsExactly(controllerLand);
        harness.assertNotOnBattlefield(player1, "Nantuko Monastery");
    }
}
