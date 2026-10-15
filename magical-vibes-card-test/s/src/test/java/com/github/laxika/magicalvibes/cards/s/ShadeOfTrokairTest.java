package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({ShadeOfTrokair.class})
class ShadeOfTrokairTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Shade of Trokair with three time counters")
    void suspendExilesWithThreeTimeCounters() {
        ShadeOfTrokair card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast and grants haste")
    void lastCounterOffersFreeCastWithHaste() {
        ShadeOfTrokair card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Shade of Trokair");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Declining the suspend cast leaves Shade of Trokair in exile")
    void decliningLastCounterLeavesShadeExiled() {
        ShadeOfTrokair card = suspendCard();

        for (int i = 0; i < 3; i++) {
            advanceToUpkeep(player1);
            harness.passBothPriorities();
        }

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == card);
    }

    @Test
    @DisplayName("The white ability gives Shade of Trokair +1/+1 until end of turn")
    void activatedAbilityBoostsPowerAndToughnessUntilEndOfTurn() {
        Permanent permanent = addReadyShadeOfTrokair(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(permanent.getPowerModifier()).isEqualTo(1);
        assertThat(permanent.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(permanent.getPowerModifier()).isZero();
        assertThat(permanent.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Suspend removes counters only during its owner's upkeep")
    void suspendCountsOnlyOwnersUpkeeps() {
        ShadeOfTrokair card = suspendCard();

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 3);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
    }

    @Test
    @DisplayName("A tapped summoning-sick Shade can activate its pump repeatedly")
    void pumpStacksWithoutTapOrSummoningSicknessRestriction() {
        Permanent permanent = addReadyShadeOfTrokair(player1);
        permanent.setSummoningSick(true);
        permanent.tap();
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(permanent.getPowerModifier()).isEqualTo(2);
        assertThat(permanent.getToughnessModifier()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
    }

    @Test
    @DisplayName("Casting Shade normally does not grant suspend haste")
    void normalCastDoesNotGrantHaste() {
        harness.setHand(player1, List.of(new ShadeOfTrokair()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent permanent = findPermanent(player1, "Shade of Trokair");
        assertThat(gqs.hasKeyword(gd, permanent, Keyword.HASTE)).isFalse();
    }

    private ShadeOfTrokair suspendCard() {
        ShadeOfTrokair card = new ShadeOfTrokair();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private Permanent addReadyShadeOfTrokair(Player player) {
        return addCreatureReady(player, new ShadeOfTrokair());
    }
}
