package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
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

        harness.passBothPriorities();
        harness.passBothPriorities();

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

        harness.passBothPriorities();
        harness.passBothPriorities();
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
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Knight of Old Benalia")).isNotNull();
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
