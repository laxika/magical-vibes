package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.j.JujuBubble;
import com.github.laxika.magicalvibes.cards.l.LongbowArcher;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WarriorsHonor.class, LongbowArcher.class, JujuBubble.class})
class WarriorsHonorTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as INSTANT_SPELL")
    void castingPutsOnStack() {
        WarriorsHonor honor = castWarriorsHonor();

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isSameAs(honor);
    }

    @Test
    @DisplayName("Resolving boosts all own creatures +1/+1")
    void resolvingBoostsAllOwnCreatures() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());

        castWarriorsHonor();
        harness.passBothPriorities();

        assertThat(first.getPowerModifier()).isEqualTo(1);
        assertThat(first.getToughnessModifier()).isEqualTo(1);
        assertThat(first.getEffectivePower()).isEqualTo(3);
        assertThat(first.getEffectiveToughness()).isEqualTo(3);
        assertThat(second.getPowerModifier()).isEqualTo(1);
        assertThat(second.getToughnessModifier()).isEqualTo(1);
        assertThat(second.getEffectivePower()).isEqualTo(3);
        assertThat(second.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not boost opponent's creatures")
    void doesNotBoostOpponentCreatures() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new LongbowArcher());

        castWarriorsHonor();
        harness.passBothPriorities();

        assertThat(ownCreature.getPowerModifier()).isEqualTo(1);
        assertThat(ownCreature.getToughnessModifier()).isEqualTo(1);
        assertThat(opponentCreature.getPowerModifier()).isZero();
        assertThat(opponentCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Boost resets at cleanup step")
    void boostResetsAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());

        castWarriorsHonor();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Affects only creatures present under its controller at resolution")
    void affectsOnlyCreaturesPresentAtResolution() {
        castWarriorsHonor();
        Permanent presentCreature = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());
        Permanent noncreature = harness.addToBattlefieldAndReturn(player1, new JujuBubble());

        harness.passBothPriorities();

        Permanent laterCreature = harness.addToBattlefieldAndReturn(player1, new LongbowArcher());

        assertThat(presentCreature.getEffectivePower()).isEqualTo(3);
        assertThat(presentCreature.getEffectiveToughness()).isEqualTo(3);
        assertThat(noncreature.getPowerModifier()).isZero();
        assertThat(noncreature.getToughnessModifier()).isZero();
        assertThat(laterCreature.getEffectivePower()).isEqualTo(2);
        assertThat(laterCreature.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Works with empty battlefield (no crash)")
    void worksWithEmptyBattlefield() {
        castWarriorsHonor();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Warrior's Honor goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        WarriorsHonor honor = castWarriorsHonor();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(honor);
    }

    private WarriorsHonor castWarriorsHonor() {
        WarriorsHonor honor = new WarriorsHonor();
        harness.castFromHand(player1, honor, "{2}{W}");
        return honor;
    }
}

