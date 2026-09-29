package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Idolized.class, GrizzlyBears.class, Forest.class})
class IdolizedTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +X/+X when it attacks alone")
    void attacksAloneBoostsByNonlandPermanents() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Forest());
        attachIdolized(creature);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(3);
        assertThat(creature.getToughnessModifier()).isEqualTo(3);
    }

    @Test
    @DisplayName("Idolized does not trigger when another creature attacks")
    void doesNotTriggerWhenNotAttackingAlone() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent other = addCreatureReady(player1, new GrizzlyBears());
        attachIdolized(creature);

        declareAttackers(player1, List.of(0, 1));
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(other.getPowerModifier()).isZero();
    }

    @Test
    @DisplayName("The attack boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachIdolized(creature);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();
        assertThat(creature.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Idolized can target only a creature")
    void cannotTargetNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new Idolized()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private void attachIdolized(Permanent creature) {
        Permanent aura = new Permanent(new Idolized());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }
}
