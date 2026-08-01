package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class AngelicProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +0/+3 until end of turn when it becomes the target of a spell")
    void gainsBoostWhenTargetedBySpell() {
        harness.addToBattlefield(player1, new AngelicProtector());
        Permanent protector = findPermanent(player1, "Angelic Protector");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, protector.getId());
        harness.passBothPriorities();

        assertThat(protector.getToughnessModifier()).isEqualTo(3);
        assertThat(protector.getEffectivePower()).isEqualTo(2);
        assertThat(protector.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(protector.getId()));
    }

    @Test
    @DisplayName("Gets +0/+3 until end of turn when it becomes the target of an ability")
    void gainsBoostWhenTargetedByAbility() {
        Permanent protector = new Permanent(new AngelicProtector());
        protector.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(protector);
        harness.addToBattlefield(player2, new ProdigalPyromancer());
        harness.addMana(player2, ManaColor.RED, 0);

        harness.activateAbility(player2, 0, null, protector.getId());
        harness.passBothPriorities();

        assertThat(protector.getToughnessModifier()).isEqualTo(3);
        assertThat(protector.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(protector.getId()));
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent protector = new Permanent(new AngelicProtector());
        protector.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(protector);
        harness.addToBattlefield(player2, new ProdigalPyromancer());

        harness.activateAbility(player2, 0, null, protector.getId());
        harness.passBothPriorities();

        assertThat(protector.getToughnessModifier()).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(protector.getToughnessModifier()).isEqualTo(0);
        assertThat(protector.getEffectiveToughness()).isEqualTo(2);
    }
}