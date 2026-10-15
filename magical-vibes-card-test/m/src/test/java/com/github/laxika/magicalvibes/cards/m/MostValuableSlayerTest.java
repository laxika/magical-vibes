package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MostValuableSlayer.class, GrizzlyBears.class})
class MostValuableSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Whenever you attack, an attacking creature gets +1/+0 and first strike")
    void boostsAndGrantsFirstStrikeToTargetAttackingCreature() {
        Permanent slayer = addCreatureReady(player1, new MostValuableSlayer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, attacker);

        declareAttackers(player1, List.of(0, 1));

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(slayer.getId(), attacker.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(originalPower + 1);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("The boost and first strike wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        addCreatureReady(player1, new MostValuableSlayer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, attacker);

        declareAttackers(player1, List.of(0, 1));
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(originalPower);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The attack trigger cannot target a creature that is not attacking")
    void cannotTargetNonAttackingCreature() {
        addCreatureReady(player1, new MostValuableSlayer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(player1, List.of(1));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(attacker.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Slayer can target itself when attacking alone")
    void canTargetItself() {
        Permanent slayer = addCreatureReady(player1, new MostValuableSlayer());
        int originalPower = gqs.getEffectivePower(gd, slayer);
        int originalToughness = gqs.getEffectiveToughness(gd, slayer);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, slayer.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, slayer)).isEqualTo(originalPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, slayer)).isEqualTo(originalToughness);
        assertThat(gqs.hasKeyword(gd, slayer, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Slayer triggers even when it does not attack")
    void triggersWhileStayingBack() {
        Permanent slayer = addCreatureReady(player1, new MostValuableSlayer());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, attacker);

        declareAttackers(player1, List.of(1));
        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attacker.getId());
        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(originalPower + 1);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, slayer, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Slayer does not trigger when only its opponent attacks")
    void doesNotTriggerForOpponentAttack() {
        addCreatureReady(player1, new MostValuableSlayer());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, attacker);

        declareAttackers(player2, List.of(0));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(originalPower);
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("The ability grants neither bonus if its target stops attacking before resolution")
    void targetMustStillBeAttackingAtResolution() {
        Permanent slayer = addCreatureReady(player1, new MostValuableSlayer());
        int originalPower = gqs.getEffectivePower(gd, slayer);

        declareAttackers(player1, List.of(0));
        harness.handlePermanentChosen(player1, slayer.getId());
        slayer.setAttacking(false);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, slayer)).isEqualTo(originalPower);
        assertThat(gqs.hasKeyword(gd, slayer, Keyword.FIRST_STRIKE)).isFalse();
    }
}
