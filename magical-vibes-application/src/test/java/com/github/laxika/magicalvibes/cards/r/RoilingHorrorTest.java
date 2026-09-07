package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(RoilingHorror.class)
class RoilingHorrorTest extends BaseCardTest {

    @Test
    void powerAndToughnessEqualLifeDifference() {
        harness.setLife(player1, 14);
        harness.setLife(player2, 9);
        Permanent horror = harness.addToBattlefieldAndReturn(player1, new RoilingHorror());

        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(5);

        harness.setLife(player2, 20);

        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(-6);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(-6);
    }

    @Test
    void suspendUsesChosenXAsTimeCounters() {
        RoilingHorror card = suspendCard(2);

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
    }

    @Test
    void timeCounterTriggerTargetsPlayerAndDrainsController() {
        suspendCard(1);
        int controllerLife = gd.playerLifeTotals.get(player1.getId());
        int targetLife = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(targetLife - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife + 1);
    }

    private RoilingHorror suspendCard(int xValue) {
        RoilingHorror card = new RoilingHorror();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);
        harness.activateHandAbility(player1, 0, null, xValue);
        return card;
    }
}
