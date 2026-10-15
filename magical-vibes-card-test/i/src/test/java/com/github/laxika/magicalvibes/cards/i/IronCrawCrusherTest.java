package com.github.laxika.magicalvibes.cards.i;
import java.util.UUID;

import com.github.laxika.magicalvibes.cards.a.ArgothianSprite;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.TurnStep;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IronCrawCrusher.class, ArgothianSprite.class})
class IronCrawCrusherTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking boosts a target attacking creature by the Crusher's power")
    void attackBoostsTargetAttackerBySourcePower() {
        Permanent crusher = addCreatureReady(player1, new IronCrawCrusher());
        Permanent sprite = addCreatureReady(player1, new ArgothianSprite());
        crusher.setPowerModifier(2);

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, sprite.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, sprite)).isEqualTo(2);
    }

    @Test
    @DisplayName("The prototype version uses its prototype power for the attack trigger")
    void prototypeUsesPrototypePower() {
        Permanent sprite = addCreatureReady(player1, new ArgothianSprite());
        harness.setHand(player1, List.of(new IronCrawCrusher()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        Permanent crusher = findPermanent(player1, "Iron-Craw Crusher");
        crusher.setSummoningSick(false);
        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isEqualTo(5);
        assertThat(gqs.getEffectiveColors(gd, crusher)).containsExactly(CardColor.GREEN);

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, sprite.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, sprite)).isEqualTo(4);
    }

    @Test
    @DisplayName("The attack trigger cannot target a creature that is not attacking")
    void rejectsNonAttackingTarget() {
        addCreatureReady(player1, new IronCrawCrusher());
        Permanent bystander = addCreatureReady(player1, new ArgothianSprite());

        declareAttackers(List.of(0));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void canTargetItselfAndBoostExpiresAtCleanup() {
        Permanent crusher = addCreatureReady(player1, new IronCrawCrusher());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crusher.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isEqualTo(6);

        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(4);
    }

    @Test
    void readsSourcePowerAtResolutionRatherThanWhenItAttacked() {
        Permanent crusher = addCreatureReady(player1, new IronCrawCrusher());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crusher.getId());
        crusher.setPowerModifier(3);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(14);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isEqualTo(6);
    }

    @Test
    void negativeSourcePowerDoesNotReduceTargetsPower() {
        Permanent crusher = addCreatureReady(player1, new IronCrawCrusher());
        crusher.setPowerModifier(-6);

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, crusher.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, crusher)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, crusher)).isEqualTo(6);
    }
}
