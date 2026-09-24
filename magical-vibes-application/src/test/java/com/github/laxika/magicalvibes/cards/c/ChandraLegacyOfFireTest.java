package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChandraLegacyOfFire.class, NicolBolasPlaneswalker.class, Mountain.class})
class ChandraLegacyOfFireTest extends BaseCardTest {

    @Test
    @DisplayName("Beginning of your end step deals damage for your planeswalkers")
    void endStepDamageCountsYourPlaneswalkers() {
        addReadyChandra(player1, 4);
        addReadyPlaneswalker(player1, 3);
        addReadyPlaneswalker(player2, 3);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("+1 adds red mana for each planeswalker you control")
    void plusOneAddsManaForControlledPlaneswalkers() {
        addReadyChandra(player1, 4);
        addReadyPlaneswalker(player1, 3);
        addReadyPlaneswalker(player2, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    @Test
    @DisplayName("0 removes loyalty counters, exiles that many cards, and grants play permission")
    void zeroRemovesCountersAndExilesMatchingNumberOfCards() {
        Permanent chandra = addReadyChandra(player1, 4);
        Permanent nicol = addReadyPlaneswalker(player1, 3);
        Card mountain = new Mountain();
        Card secondCard = new Mountain();
        harness.setLibrary(player1, List.of(mountain, secondCard));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(chandra.getId(), nicol.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(chandra.getId(), nicol.getId()));

        assertThat(chandra.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(nicol.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(mountain, secondCard);
        assertThat(gd.exilePlayPermissions).containsEntry(mountain.getId(), player1.getId());
        assertThat(gd.exilePlayPermissions).containsEntry(secondCard.getId(), player1.getId());
    }

    private Permanent addReadyChandra(Player player, int loyalty) {
        Permanent chandra = new Permanent(new ChandraLegacyOfFire());
        chandra.setCounterCount(CounterType.LOYALTY, loyalty);
        chandra.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(chandra);
        return chandra;
    }

    private Permanent addReadyPlaneswalker(Player player, int loyalty) {
        Permanent planeswalker = new Permanent(new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, loyalty);
        planeswalker.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(planeswalker);
        return planeswalker;
    }
}
