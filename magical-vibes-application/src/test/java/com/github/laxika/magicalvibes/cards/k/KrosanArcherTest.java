package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrosanArcher.class, Forest.class, AvenFisher.class})
class KrosanArcherTest extends BaseCardTest {

    @Test
    void payingGreenAndDiscardingACardGivesKrosanArcherPlusZeroPlusTwoUntilEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent archer = harness.addToBattlefieldAndReturn(player1, new KrosanArcher());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, archer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, archer)).isEqualTo(5);
        harness.assertInGraveyard(player1, "Forest");

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, archer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, archer)).isEqualTo(3);
    }

    @Test
    void cannotActivateWithoutACardInHand() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new KrosanArcher());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotActivateWithoutGreenMana() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefieldAndReturn(player1, new KrosanArcher());
        harness.setHand(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");

        harness.assertInHand(player1, "Forest");
    }

    @Test
    void canDiscardANonlandCardAndPaysTheCostBeforeTheBoostResolves() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent archer = harness.addToBattlefieldAndReturn(player1, new KrosanArcher());
        harness.setHand(player1, List.of(new KrosanArcher()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);

        harness.assertNotInHand(player1, "Krosan Archer");
        harness.assertInGraveyard(player1, "Krosan Archer");
        assertThat(gqs.getEffectiveToughness(gd, archer)).isEqualTo(3);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, archer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, archer)).isEqualTo(5);
    }

    @Test
    void repeatedActivationsStackAndDoNotBoostAnotherArcher() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent archer = harness.addToBattlefieldAndReturn(player1, new KrosanArcher());
        Permanent otherArcher = harness.addToBattlefieldAndReturn(player1, new KrosanArcher());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, archer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, archer)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, otherArcher)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }

    @Test
    void canActivateWhileTappedAndSummoningSick() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent archer = harness.addToBattlefieldAndReturn(player1, new KrosanArcher());
        archer.tap();
        archer.setSummoningSick(true);
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, archer)).isEqualTo(5);
        assertThat(archer.isTapped()).isTrue();
    }

    @Test
    void reachAllowsBlockingAFlyingCreature() {
        Permanent archer = harness.addToBattlefieldAndReturn(player1, new KrosanArcher());
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new AvenFisher());

        assertThat(bls.canBlockAttacker(gd, archer, attacker,
                gd.playerBattlefields.get(player1.getId()))).isTrue();
    }
}
