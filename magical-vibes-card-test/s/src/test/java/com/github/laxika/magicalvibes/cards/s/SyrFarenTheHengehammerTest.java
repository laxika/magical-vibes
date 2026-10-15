package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WildbornPreserver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SyrFarenTheHengehammer.class, WildbornPreserver.class})
class SyrFarenTheHengehammerTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger targets another attacking creature")
    void attackTriggerRestrictsTargets() {
        Permanent syrFaren = addReadyCreature(new SyrFarenTheHengehammer());
        Permanent attackingCreature = addReadyCreature(new WildbornPreserver());
        Permanent nonAttackingCreature = addReadyCreature(new WildbornPreserver());

        declareAttackers(List.of(0, 1));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(attackingCreature.getId())
                .doesNotContain(syrFaren.getId(), nonAttackingCreature.getId());
    }

    @Test
    @DisplayName("Attack trigger gives the target +X/+X where X is Syr Faren's power")
    void attackTriggerUsesSourcePower() {
        Permanent syrFaren = addReadyCreature(new SyrFarenTheHengehammer());
        syrFaren.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent attackingCreature = addReadyCreature(new WildbornPreserver());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attackingCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attackingCreature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, attackingCreature)).isEqualTo(5);
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void attackBoostWearsOffAtEndOfTurn() {
        addReadyCreature(new SyrFarenTheHengehammer());
        Permanent attackingCreature = addReadyCreature(new WildbornPreserver());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, attackingCreature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attackingCreature)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attackingCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, attackingCreature)).isEqualTo(2);
    }

    @Test
    void usesPowerAtResolutionRatherThanWhenAttacking() {
        Permanent syrFaren = addReadyCreature(new SyrFarenTheHengehammer());
        Permanent target = addReadyCreature(new WildbornPreserver());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        syrFaren.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
    }

    @Test
    void negativeSourcePowerGivesNoBoost() {
        Permanent syrFaren = addReadyCreature(new SyrFarenTheHengehammer());
        Permanent target = addReadyCreature(new WildbornPreserver());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        syrFaren.setPowerModifier(-3);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void usesLastKnownPowerWhenSourceDiesBeforeResolution() {
        Permanent syrFaren = addReadyCreature(new SyrFarenTheHengehammer());
        Permanent target = addReadyCreature(new WildbornPreserver());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        syrFaren.setPowerModifier(3);
        syrFaren.setToughnessModifier(-2);
        harness.runStateBasedActions();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(syrFaren);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(7);
    }

    @Test
    void targetThatStopsAttackingReceivesNoBoost() {
        addReadyCreature(new SyrFarenTheHengehammer());
        Permanent target = addReadyCreature(new WildbornPreserver());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, target.getId());
        target.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void attackingAloneDoesNotBoostItself() {
        Permanent syrFaren = addReadyCreature(new SyrFarenTheHengehammer());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, syrFaren)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, syrFaren)).isEqualTo(2);
    }

    private Permanent addReadyCreature(Card card) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player1, card);
        permanent.setSummoningSick(false);
        return permanent;
    }
}
