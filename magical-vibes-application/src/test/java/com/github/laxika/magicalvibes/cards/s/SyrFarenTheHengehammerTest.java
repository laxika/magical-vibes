package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({SyrFarenTheHengehammer.class, GrizzlyBears.class})
class SyrFarenTheHengehammerTest extends BaseCardTest {

    @Test
    @DisplayName("Attack trigger targets another attacking creature")
    void attackTriggerRestrictsTargets() {
        Permanent syrFaren = addReadyCreature(new SyrFarenTheHengehammer());
        Permanent attackingCreature = addReadyCreature(new GrizzlyBears());
        Permanent nonAttackingCreature = addReadyCreature(new GrizzlyBears());

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
        Permanent attackingCreature = addReadyCreature(new GrizzlyBears());

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
        Permanent attackingCreature = addReadyCreature(new GrizzlyBears());

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

    private Permanent addReadyCreature(Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(permanent);
        return permanent;
    }
}
