package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class IchorclawMyrTest extends BaseCardTest {

    @Test
    @DisplayName("Ichorclaw Myr has ON_BECOMES_BLOCKED effect with BoostSelfEffect +2/+2")
    void hasOnBecomesBlockedEffect() {
        IchorclawMyr card = new IchorclawMyr();

        assertThat(card.getEffects(EffectSlot.ON_BECOMES_BLOCKED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_BLOCKED).getFirst()).isInstanceOf(BoostSelfEffect.class);
        BoostSelfEffect effect = (BoostSelfEffect) card.getEffects(EffectSlot.ON_BECOMES_BLOCKED).getFirst();
        assertThat(effect.powerBoost()).isEqualTo(2);
        assertThat(effect.toughnessBoost()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Ichorclaw Myr becomes blocked, a triggered ability is pushed onto the stack")
    void becomesBlockedPushesTriggerOntoStack() {
        Permanent myrPerm = addMyrReady(player1);
        myrPerm.setAttacking(true);

        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(entry ->
                entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && entry.getCard().getName().equals("Ichorclaw Myr")
                        && entry.getSourcePermanentId().equals(myrPerm.getId()));
    }

    @Test
    @DisplayName("Resolving becomes-blocked trigger gives +2/+2 until end of turn")
    void becomesBlockedTriggerGivesPlusTwoPlusTwo() {
        Permanent myrPerm = addMyrReady(player1);
        myrPerm.setAttacking(true);

        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(myrPerm.getPowerModifier()).isEqualTo(2);
        assertThat(myrPerm.getToughnessModifier()).isEqualTo(2);
        assertThat(myrPerm.getEffectivePower()).isEqualTo(3);
        assertThat(myrPerm.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Becomes-blocked trigger fires only once even with multiple blockers")
    void becomesBlockedFiresOnceWithMultipleBlockers() {
        Permanent myrPerm = addMyrReady(player1);
        myrPerm.getCard().setPower(4);
        myrPerm.getCard().setToughness(4);
        myrPerm.setAttacking(true);

        addReadyBears(player2);
        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long myrTriggerCount = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Ichorclaw Myr"))
                .count();
        assertThat(myrTriggerCount).isEqualTo(1);

        harness.passBothPriorities();

        // Only +2/+2, not +4/+4
        assertThat(myrPerm.getPowerModifier()).isEqualTo(2);
        assertThat(myrPerm.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("+2/+2 modifier resets at end of turn cleanup")
    void modifierResetsAtEndOfTurn() {
        Permanent myrPerm = addMyrReady(player1);
        myrPerm.getCard().setPower(4);
        myrPerm.getCard().setToughness(4);
        myrPerm.setAttacking(true);

        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(myrPerm.getPowerModifier()).isEqualTo(2);
        assertThat(myrPerm.getToughnessModifier()).isEqualTo(2);

        // Advance to cleanup step
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(myrPerm.getPowerModifier()).isEqualTo(0);
        assertThat(myrPerm.getToughnessModifier()).isEqualTo(0);
    }

    private Permanent addMyrReady(Player player) {
        Permanent perm = new Permanent(new IchorclawMyr());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
