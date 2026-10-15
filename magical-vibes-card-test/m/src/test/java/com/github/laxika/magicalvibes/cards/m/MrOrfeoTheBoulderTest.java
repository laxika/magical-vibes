package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.ParagonOfModernity;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MrOrfeoTheBoulder.class, ParagonOfModernity.class, Forest.class})
class MrOrfeoTheBoulderTest extends BaseCardTest {

    @Test
    void doublesTargetCreaturePowerOnceWhenYouAttack() {
        addCreatureReady(player1, new MrOrfeoTheBoulder());
        Permanent target = addCreatureReady(player1, new ParagonOfModernity());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void attackTriggerFiresOnceForMultipleAttackersAndCanTargetOpponentCreature() {
        addCreatureReady(player1, new MrOrfeoTheBoulder());
        addCreatureReady(player1, new ParagonOfModernity());
        Permanent opponentTarget = addCreatureReady(player2, new ParagonOfModernity());

        declareAttackers(List.of(0, 1));
        harness.handlePermanentChosen(player1, opponentTarget.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentTarget)).isEqualTo(4);
    }

    @Test
    void attackTriggerOnlyAllowsCreatureTargets() {
        addCreatureReady(player1, new MrOrfeoTheBoulder());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        declareAttackers(List.of(0));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(forest.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void powerDoublingWearsOffAtEndOfTurn() {
        addCreatureReady(player1, new MrOrfeoTheBoulder());
        Permanent target = addCreatureReady(player1, new ParagonOfModernity());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
    }

    @Test
    void triggersWhenOrfeoDoesNotAttackAndCanTargetItself() {
        Permanent orfeo = addCreatureReady(player1, new MrOrfeoTheBoulder());
        addCreatureReady(player1, new ParagonOfModernity());

        declareAttackers(List.of(1));
        harness.handlePermanentChosen(player1, orfeo.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, orfeo)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, orfeo)).isEqualTo(4);
    }

    @Test
    void doesNotTriggerWhenOpponentAttacks() {
        Permanent orfeo = addCreatureReady(player1, new MrOrfeoTheBoulder());
        Permanent attacker = addCreatureReady(player2, new ParagonOfModernity());

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, orfeo)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(2);
    }

    @Test
    void doesNotTriggerWhenNoAttackersAreDeclared() {
        Permanent orfeo = addCreatureReady(player1, new MrOrfeoTheBoulder());

        declareAttackers(List.of());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, orfeo)).isEqualTo(2);
    }

    @Test
    void doublesPowerAtResolutionRatherThanWhenTriggered() {
        addCreatureReady(player1, new MrOrfeoTheBoulder());
        Permanent target = addCreatureReady(player1, new ParagonOfModernity());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }
}
