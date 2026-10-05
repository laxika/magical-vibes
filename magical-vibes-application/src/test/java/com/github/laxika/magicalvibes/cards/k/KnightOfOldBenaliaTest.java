package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnightOfOldBenalia.class, GrizzlyBears.class})
class KnightOfOldBenaliaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB boosts other creatures you control, but not itself or opposing creatures")
    void etbBoostsOtherOwnCreaturesOnly() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castKnight();

        resolveAllTriggers();

        Permanent knight = findPermanent(player1, "Knight of Old Benalia");
        assertThat(ownBears.getPowerModifier()).isEqualTo(1);
        assertThat(ownBears.getToughnessModifier()).isEqualTo(1);
        assertThat(knight.getPowerModifier()).isEqualTo(0);
        assertThat(knight.getToughnessModifier()).isEqualTo(0);
        assertThat(opposingBears.getPowerModifier()).isEqualTo(0);
        assertThat(opposingBears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("ETB boost wears off at end of turn")
    void etbBoostWearsOff() {
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castKnight();

        resolveAllTriggers();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(ownBears.getPowerModifier()).isEqualTo(0);
        assertThat(ownBears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Suspend exiles Knight of Old Benalia with five time counters")
    void suspendExilesWithFiveTimeCounters() {
        KnightOfOldBenalia card = suspendKnight();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast")
    void lastSuspendCounterOffersFreeCast() {
        KnightOfOldBenalia card = suspendKnight();

        for (int i = 0; i < 4; i++) {
            removeOneTimeCounter();
        }

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        Permanent knight = findPermanent(player1, "Knight of Old Benalia");
        assertThat(knight).isNotNull();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.HASTE)).isTrue();
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(card);
    }

    @Test
    @DisplayName("Suspend removes counters only on its owner's upkeep and on trigger resolution")
    void suspendCounterRemovalTiming() {
        KnightOfOldBenalia card = suspendKnight();

        advanceToUpkeep(player2);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);

        advanceToUpkeep(player1);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 5);
        resolveAllTriggers();
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 4);
    }

    @Test
    @DisplayName("Declining the suspend cast leaves the card exiled without a later cast offer")
    void mayDeclineSuspendCast() {
        KnightOfOldBenalia card = suspendKnight();
        for (int i = 0; i < 5; i++) {
            advanceToUpkeep(player1);
            resolveAllTriggers();
        }

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(card.getId());
        harness.assertNotOnBattlefield(player1, "Knight of Old Benalia");

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("ETB boosts creatures present when it resolves, but not creatures arriving later")
    void etbLocksInCreaturesAtResolution() {
        castKnight();
        harness.passBothPriorities();
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new KnightOfOldBenalia());

        resolveAllTriggers();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new KnightOfOldBenalia());

        assertThat(beforeResolution.getPowerModifier()).isEqualTo(1);
        assertThat(beforeResolution.getToughnessModifier()).isEqualTo(1);
        assertThat(afterResolution.getPowerModifier()).isZero();
        assertThat(afterResolution.getToughnessModifier()).isZero();
    }

    private void castKnight() {
        harness.setHand(player1, List.of(new KnightOfOldBenalia()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private KnightOfOldBenalia suspendKnight() {
        KnightOfOldBenalia card = new KnightOfOldBenalia();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }

    private void removeOneTimeCounter() {
        advanceToUpkeep(player1);
        harness.passBothPriorities();
    }
}
