package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.e.ErtaiTheCorrupted;
import com.github.laxika.magicalvibes.cards.f.ForsakenCity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlaneswalkersScorn.class, ErtaiTheCorrupted.class, ForsakenCity.class})
class PlaneswalkersScornTest extends BaseCardTest {

    @Test
    void debuffsTargetCreatureByRevealedManaValueUntilEndOfTurn() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersScorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ErtaiTheCorrupted());
        PlaneswalkersScorn revealed = new PlaneswalkersScorn();
        harness.setHand(player2, List.of(revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower - 3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness - 3);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealed);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
    }

    @Test
    void emptyHandDoesNotDebuffCreature() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersScorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ErtaiTheCorrupted());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
    }

    @Test
    void requiresOpponentAndCreatureTargets() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersScorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ErtaiTheCorrupted());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player1.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresCreatureTarget() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersScorn());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresThreeGenericAndOneBlackMana() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersScorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ErtaiTheCorrupted());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotBeActivatedOutsideSorcerySpeed() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersScorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ErtaiTheCorrupted());
        harness.setHand(player2, List.of(new PlaneswalkersScorn()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void landRevealHasZeroManaValue() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersScorn());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ErtaiTheCorrupted());
        ForsakenCity revealed = new ForsakenCity();
        harness.setHand(player2, List.of(revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealed);
    }
}
