package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.r.RadiantFountain;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FirstResponse.class, LightningStrike.class, RadiantFountain.class})
class FirstResponseTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Soldier during each upkeep when its controller lost life last turn")
    void createsSoldierAfterControllerLostLifeLastTurn() {
        harness.addToBattlefield(player1, new FirstResponse());
        harness.setHand(player2, java.util.List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        var soldier = findPermanent(player1, "Soldier").getCard();
        assertThat(soldier.isToken()).isTrue();
        assertThat(soldier.getType()).isEqualTo(CardType.CREATURE);
        assertThat(soldier.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(soldier.getSubtypes()).contains(CardSubtype.SOLDIER);
        assertThat(soldier.getPower()).isEqualTo(1);
        assertThat(soldier.getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not create a Soldier when its controller did not lose life last turn")
    void doesNotCreateSoldierWithoutLifeLoss() {
        harness.addToBattlefield(player1, new FirstResponse());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Soldier")).isZero();
    }

    @Test
    void createsSoldierOnOpponentsUpkeepAndStopsAfterTurnWithoutLifeLoss() {
        harness.addToBattlefield(player1, new FirstResponse());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, java.util.List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player1.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
        assertThat(countPermanents(player2, "Soldier")).isZero();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }

    @Test
    void opponentsLifeLossDoesNotTriggerAbility() {
        harness.addToBattlefield(player1, new FirstResponse());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, java.util.List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, player2.getId());

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Soldier")).isZero();
    }

    @Test
    void lifeLostDuringCurrentUpkeepDoesNotTriggerAbility() {
        harness.addToBattlefield(player1, new FirstResponse());
        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        harness.setHand(player2, java.util.List.of(new LightningStrike()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(countPermanents(player1, "Soldier")).isZero();
    }

    @Test
    void gainingLifeDoesNotUndoLastTurnsLifeLoss() {
        harness.addToBattlefield(player1, new FirstResponse());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, java.util.List.of(new LightningStrike(), new RadiantFountain()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.playLand(player1, 0);
        resolveAllTriggers();
        harness.assertLife(player1, 19);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }

    @Test
    void payingLifeAlsoQualifiesAsLifeLoss() {
        harness.addToBattlefield(player1, new FirstResponse());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.inMutationScope(() -> harness.getLifeSupport()
                .applyLifePayment(gd, player1.getId(), 1, "Life payment"));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }
}
