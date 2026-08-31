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
