package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Fireslinger;
import com.github.laxika.magicalvibes.cards.s.SearingTouch;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelicProtector.class, Fireslinger.class, SearingTouch.class})
class AngelicProtectorTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +0/+3 until end of turn when it becomes the target of a spell")
    void gainsBoostWhenTargetedBySpell() {
        Permanent protector = harness.addToBattlefieldAndReturn(player1, new AngelicProtector());
        harness.setHand(player2, List.of(new SearingTouch()));
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
    @DisplayName("Gets +0/+3 for each time it becomes a target in the same turn")
    void gainsBoostEachTimeTargeted() {
        Permanent protector = harness.addToBattlefieldAndReturn(player1, new AngelicProtector());
        harness.setHand(player2, List.of(new SearingTouch(), new SearingTouch()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castInstant(player2, 0, protector.getId());
        harness.passBothPriorities();
        harness.castInstant(player2, 0, protector.getId());
        harness.passBothPriorities();

        assertThat(protector.getToughnessModifier()).isEqualTo(6);
        assertThat(protector.getEffectivePower()).isEqualTo(2);
        assertThat(protector.getEffectiveToughness()).isEqualTo(8);
    }

    @Test
    @DisplayName("Gets +0/+3 until end of turn when it becomes the target of an ability")
    void gainsBoostWhenTargetedByAbility() {
        Permanent protector = addCreatureReady(player1, new AngelicProtector());
        addCreatureReady(player2, new Fireslinger());

        harness.activateAbility(player2, 0, null, protector.getId());
        harness.passBothPriorities();

        assertThat(protector.getToughnessModifier()).isEqualTo(3);
        assertThat(protector.getEffectiveToughness()).isEqualTo(5);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(protector.getId()));
    }

    @Test
    @DisplayName("Gets +0/+3 when targeted by an ability it controls")
    void gainsBoostWhenTargetedByOwnAbility() {
        addCreatureReady(player1, new Fireslinger());
        Permanent protector = addCreatureReady(player1, new AngelicProtector());

        harness.activateAbility(player1, 0, null, protector.getId());
        harness.passBothPriorities();

        assertThat(protector.getToughnessModifier()).isEqualTo(3);
        assertThat(protector.getEffectivePower()).isEqualTo(2);
        assertThat(protector.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent protector = addCreatureReady(player1, new AngelicProtector());
        addCreatureReady(player2, new Fireslinger());

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
