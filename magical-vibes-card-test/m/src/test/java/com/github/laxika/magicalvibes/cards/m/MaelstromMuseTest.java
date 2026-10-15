package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.e.Expel;
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
        MindSpring.class, MultipleChoice.class, Expel.class})
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
        harness.castAndResolveInstant(player1, 0);

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

    @Test
    @DisplayName("Casting a creature leaves the reduction for the next sorcery")
    void successfullyCastCreatureDoesNotConsumeReduction() {
        addReadyMuse();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MaelstromMuse(), new MultipleChoice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.castSorcery(player1, 0, 2);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Multiple Choice");
    }

    @Test
    @DisplayName("Multiple attack triggers combine on the same next spell")
    void multipleMusesCombineReductions() {
        addReadyMuse();
        addReadyMuse();
        declareAttackers(List.of(0, 1));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MultipleChoice(), new MultipleChoice()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 3);
        resolveAllTriggers();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 1))
                .isInstanceOf(IllegalStateException.class);
        harness.castSorcery(player1, 0, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("A large reduction cannot pay colored mana")
    void reductionCannotPayColoredMana() {
        Permanent muse = addReadyMuse();
        muse.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 3))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 3);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An opponent's spell does not receive or consume the reduction")
    void opponentDoesNotReceiveReduction() {
        addReadyMuse();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.setHand(player2, List.of(new AngelsMercy()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player2, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player2, 0);
        harness.setHand(player1, List.of(new AngelsMercy()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An unused reduction expires at the end of the turn")
    void unusedReductionExpires() {
        addReadyMuse();
        declareAttackers(List.of(0));
        resolveAllTriggers();
        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new AngelsMercy()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("An exiled Muse uses its power immediately before leaving")
    void usesLastKnownPowerAfterExile() {
        Permanent muse = addReadyMuse();
        declareAttackers(List.of(0));
        muse.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player2, List.of(new Expel()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player2, 0, muse.getId());
        harness.assertNotOnBattlefield(player1, "Maelstrom Muse");
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MultipleChoice()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castSorcery(player1, 0, 5);

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent addReadyMuse() {
        return addCreatureReady(player1, new MaelstromMuse());
    }
}
