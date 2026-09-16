package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.ForsakenCity;
import com.github.laxika.magicalvibes.cards.g.Gainsay;
import com.github.laxika.magicalvibes.cards.s.StoneKavu;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlaneswalkersFavor.class, StoneKavu.class, Gainsay.class, ForsakenCity.class})
class PlaneswalkersFavorTest extends BaseCardTest {

    @Test
    void pumpsTargetCreatureByRevealedManaValueUntilEndOfTurn() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersFavor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StoneKavu());
        Gainsay revealed = new Gainsay();
        harness.setHand(player2, List.of(revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness + 2);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealed);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
    }

    @Test
    void emptyHandDoesNotPumpCreature() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersFavor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StoneKavu());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
    }

    @Test
    void requiresOpponentAndCreatureTargets() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersFavor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StoneKavu());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player1.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresCreatureTarget() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersFavor());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresGreenMana() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersFavor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StoneKavu());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void requiresThreeGenericMana() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersFavor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StoneKavu());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canTargetCreatureControlledByOpponent() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersFavor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StoneKavu());
        Gainsay revealed = new Gainsay();
        harness.setHand(player2, List.of(revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness + 2);
    }

    @Test
    void landRevealHasZeroManaValue() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersFavor());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new StoneKavu());
        ForsakenCity revealed = new ForsakenCity();
        harness.setHand(player2, List.of(revealed));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);

        harness.activateAbilityWithMultiTargets(player1, 0, 0, List.of(player2.getId(), target.getId()));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(originalToughness);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(revealed);
    }

    @Test
    void cannotBeActivatedOutsideSorcerySpeed() {
        harness.addToBattlefieldAndReturn(player1, new PlaneswalkersFavor());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new StoneKavu());
        harness.setHand(player2, List.of(new Gainsay()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 0, List.of(player2.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
