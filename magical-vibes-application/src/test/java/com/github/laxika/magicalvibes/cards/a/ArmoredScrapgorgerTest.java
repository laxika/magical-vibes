package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ArmoredScrapgorgerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Armored Scrapgorger targets a graveyard card and adds an oil counter")
    void tappingExilesGraveyardCardAndAddsOilCounter() {
        Permanent scrapgorger = harness.addToBattlefieldAndReturn(player1, new ArmoredScrapgorger());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        tap(scrapgorger);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .contains(bears.getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(card -> card.getId().equals(bears.getId()));
        assertThat(scrapgorger.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    @DisplayName("The tap trigger does nothing if its graveyard target is gone on resolution")
    void triggerDoesNothingWhenTargetIsGone() {
        Permanent scrapgorger = harness.addToBattlefieldAndReturn(player1, new ArmoredScrapgorger());
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        tap(scrapgorger);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        gd.playerGraveyards.get(player2.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).noneMatch(card -> card.getId().equals(bears.getId()));
        assertThat(scrapgorger.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    @DisplayName("Armored Scrapgorger does not trigger when another permanent becomes tapped")
    void tappingAnotherPermanentDoesNotTrigger() {
        harness.addToBattlefield(player1, new ArmoredScrapgorger());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        tap(bears);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Three oil counters give Armored Scrapgorger +3/+0")
    void threeOilCountersGrantPower() {
        Permanent scrapgorger = harness.addToBattlefieldAndReturn(player1, new ArmoredScrapgorger());

        scrapgorger.setCounterCount(CounterType.OIL, 2);
        assertThat(gqs.getEffectivePower(gd, scrapgorger)).isZero();

        scrapgorger.setCounterCount(CounterType.OIL, 3);
        assertThat(gqs.getEffectivePower(gd, scrapgorger)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, scrapgorger)).isEqualTo(3);
    }

    @Test
    @DisplayName("The mana ability adds one mana of the chosen color")
    void manaAbilityAddsChosenColor() {
        Permanent scrapgorger = harness.addToBattlefieldAndReturn(player1, new ArmoredScrapgorger());
        scrapgorger.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Tapping for mana still queues the graveyard trigger")
    void manaAbilityAlsoQueuesTapTrigger() {
        Permanent scrapgorger = harness.addToBattlefieldAndReturn(player1, new ArmoredScrapgorger());
        scrapgorger.setSummoningSick(false);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(bears)));

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.getPlayerExiledCards(player2.getId())).anyMatch(card -> card.getId().equals(bears.getId()));
        assertThat(scrapgorger.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    private void tap(Permanent permanent) {
        permanent.tap();
        harness.inMutationScope(
                () -> harness.getTriggerCollectionService().checkEnchantedPermanentTapTriggers(gd, permanent));
    }
}
