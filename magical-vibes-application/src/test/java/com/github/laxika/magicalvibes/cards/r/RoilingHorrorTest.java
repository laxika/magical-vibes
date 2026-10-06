package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.p.PlatinumAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RoilingHorror.class, PlatinumAngel.class})
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
    void suspendRejectsZeroX() {
        RoilingHorror card = new RoilingHorror();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Suspend X requires X to be at least 1");
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
    }

    @Test
    void timeCounterTriggerFiresWhenNonLastCounterIsRemoved() {
        RoilingHorror card = suspendCard(2);
        int controllerLife = gd.playerLifeTotals.get(player1.getId());
        int targetLife = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(targetLife - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife + 1);
    }

    @Test
    void lastCounterMayCastRoilingHorrorWithHaste() {
        RoilingHorror card = suspendCard(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        Permanent horror = findPermanent(player1, "Roiling Horror");
        assertThat(gqs.hasKeyword(gd, horror, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
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

    @Test
    void negativeOpponentLifeTotalIncreasesPowerAndToughness() {
        harness.addToBattlefield(player2, new PlatinumAngel());
        harness.setLife(player1, 20);
        harness.setLife(player2, -3);
        Permanent horror = harness.addToBattlefieldAndReturn(player1, new RoilingHorror());
        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(23);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(23);
    }

    @Test
    void powerAndToughnessUpdateWhenControllerLifeChanges() {
        harness.setLife(player1, 15);
        harness.setLife(player2, 10);
        Permanent horror = harness.addToBattlefieldAndReturn(player1, new RoilingHorror());

        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(5);

        harness.setLife(player1, 18);

        assertThat(gqs.getEffectivePower(gd, horror)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, horror)).isEqualTo(8);
    }

    @Test
    void normallyCastHorrorDiesWhenLifeTotalsAreEqual() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.castFromHand(player1, new RoilingHorror(), "{3}{B}{B}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Roiling Horror");
        harness.assertInGraveyard(player1, "Roiling Horror");
    }

    @Test
    void opponentUpkeepDoesNotRemoveTimeCounter() {
        RoilingHorror card = suspendCard(2);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void timeCounterTriggerCanTargetItsController() {
        RoilingHorror card = suspendCard(2);
        harness.setLife(player1, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 1);
        harness.assertLife(player1, 1);
        harness.assertLife(player2, 20);
        assertThat(gd.status).isNotEqualTo(com.github.laxika.magicalvibes.model.GameStatus.FINISHED);
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
