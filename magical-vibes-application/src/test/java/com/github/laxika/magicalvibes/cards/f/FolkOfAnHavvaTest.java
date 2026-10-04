package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FolkOfAnHavva.class})
class FolkOfAnHavvaTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking gives +2/+0 until end of turn")
    void blockTriggerGivesPlusTwoPlusZero() {
        Permanent folk = block();

        assertThat(folk.getPowerModifier()).isEqualTo(2);
        assertThat(folk.getToughnessModifier()).isEqualTo(0);
        assertThat(folk.getEffectivePower()).isEqualTo(3);
        assertThat(folk.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void modifierResetsAtEndOfTurn() {
        Permanent folk = block();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(folk.getPowerModifier()).isEqualTo(0);
        assertThat(folk.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("No boost while it is not blocking")
    void noBoostWithoutBlocking() {
        Permanent folk = addCreatureReady(player2, new FolkOfAnHavva());

        assertThat(folk.getPowerModifier()).isEqualTo(0);
        assertThat(folk.getEffectivePower()).isEqualTo(1);
    }

    @Test
    @DisplayName("Blocking multiple creatures gives only one boost")
    void blockingMultipleCreaturesBoostsOnlyOnce() {
        Permanent firstAttacker = addCreatureReady(player1, new FolkOfAnHavva());
        firstAttacker.setAttacking(true);
        Permanent secondAttacker = addCreatureReady(player1, new FolkOfAnHavva());
        secondAttacker.setAttacking(true);

        FolkOfAnHavva card = new FolkOfAnHavva();
        card.addEffect(EffectSlot.STATIC, new CanBlockAnyNumberOfCreaturesEffect());
        Permanent folk = addCreatureReady(player2, card);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1)));
        harness.passUntil(TurnStep.COMBAT_DAMAGE);

        assertThat(folk.getPowerModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Each blocking copy gets its own boost only after its trigger resolves")
    void eachBlockingCopyTriggersIndependently() {
        addCreatureReady(player1, new FolkOfAnHavva());
        addCreatureReady(player1, new FolkOfAnHavva());
        Permanent firstBlocker = addCreatureReady(player2, new FolkOfAnHavva());
        Permanent secondBlocker = addCreatureReady(player2, new FolkOfAnHavva());
        declareAttackersAndPrepareBlockers(List.of(0, 1));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));

            assertThat(gd.stack).hasSize(2);
            assertThat(gqs.getEffectivePower(gd, firstBlocker)).isEqualTo(1);
            assertThat(gqs.getEffectivePower(gd, secondBlocker)).isEqualTo(1);

            resolveAllTriggers();

            assertThat(gqs.getEffectivePower(gd, firstBlocker)).isEqualTo(3);
            assertThat(gqs.getEffectivePower(gd, secondBlocker)).isEqualTo(3);
            assertThat(gqs.getEffectiveToughness(gd, firstBlocker)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, secondBlocker)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Attacking and becoming blocked do not boost the attacking copy")
    void attackingCopyDoesNotGetBlockBoost() {
        Permanent attacker = addCreatureReady(player1, new FolkOfAnHavva());
        Permanent blocker = addCreatureReady(player2, new FolkOfAnHavva());
        declareAttackersAndPrepareBlockers(List.of(0));

        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, () -> {
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            resolveAllTriggers();

            assertThat(gqs.getEffectivePower(gd, attacker)).isEqualTo(1);
            assertThat(gqs.getEffectivePower(gd, blocker)).isEqualTo(3);
        });
    }

    private Permanent block() {
        Permanent folk = addCreatureReady(player2, new FolkOfAnHavva());

        Permanent attacker = addCreatureReady(player1, new FolkOfAnHavva());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        return folk;
    }
}
