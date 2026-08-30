package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
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

@CardUsed({MaelstromMuse.class, AngelsMercy.class, Divination.class, GrizzlyBears.class,
        MindSpring.class})
class MaelstromMuseTest extends BaseCardTest {

    @Test
    @DisplayName("Reduces the next instant by the Muse's power")
    void reducesNextInstantByPower() {
        addReadyMuse();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.setHand(player1, List.of(new AngelsMercy()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Angel's Mercy");
    }

    @Test
    @DisplayName("The reduction is consumed by the first matching spell")
    void reductionIsConsumedByFirstMatchingSpell() {
        addReadyMuse();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.setHand(player1, List.of(new AngelsMercy(), new AngelsMercy()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Nonmatching spells do not consume the reduction")
    void nonmatchingSpellDoesNotConsumeReduction() {
        addReadyMuse();
        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.setHand(player1, List.of(new GrizzlyBears(), new Divination()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Divination");
    }

    @Test
    @DisplayName("Power is evaluated when the attack trigger resolves")
    void powerIsEvaluatedOnResolution() {
        Permanent muse = addReadyMuse();
        declareAttackers(List.of(0));
        muse.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        resolveAllTriggers();
        muse.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MindSpring()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castSorcery(player1, 0, 4);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Mind Spring");
    }

    private Permanent addReadyMuse() {
        return addCreatureReady(player1, new MaelstromMuse());
    }
}
