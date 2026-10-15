package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.e.EssenceExtraction;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AngelOfInvention.class, GrizzlyBears.class, EssenceExtraction.class})
class AngelOfInventionTest extends BaseCardTest {

    @Test
    @DisplayName("Fabricate mode puts two +1/+1 counters on Angel of Invention")
    void fabricateCountersMode() {
        castAngel(0);
        resolveAllTriggers();

        Permanent angel = findPermanent(player1, "Angel of Invention");
        assertThat(angel.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fabricate mode creates two 1/1 colorless Servo artifact creature tokens")
    void fabricateServoMode() {
        castAngel(1);
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, false);
        }

        List<Permanent> servos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .toList();

        assertThat(servos).hasSize(2);
        assertThat(servos).allSatisfy(servo -> {
            assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(2);
            assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(2);
        });
    }

    @Test
    @DisplayName("Other creatures you control get +1/+1")
    void boostsOtherCreaturesYouControl() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent angel = addCreatureReady(player1, new AngelOfInvention());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, angel)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, angel)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, opposingBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opposingBears)).isEqualTo(2);
    }

    private void castAngel(int mode) {
        harness.setHand(player1, List.of(new AngelOfInvention()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, mode == 0);
    }

    @Test
    @DisplayName("Fabricate choice is offered when the triggered ability resolves")
    void fabricateChoiceIsDeferredUntilResolution() {
        harness.setHand(player1, List.of(new AngelOfInvention()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        Permanent angel = findPermanent(player1, "Angel of Invention");
        assertThat(angel.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();

        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO)))
                .hasSize(2);
        assertThat(angel.getCounterCount(com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE))
                .isZero();
    }

    @Test
    @DisplayName("Fabricate creates Servos if the Angel leaves before the trigger resolves")
    void fabricateCreatesServosWhenSourceIsGone() {
        harness.setHand(player1, List.of(new AngelOfInvention()));
        harness.addMana(player1, ManaColor.WHITE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent angel = findPermanent(player1, "Angel of Invention");
        harness.setHand(player2, List.of(new EssenceExtraction()));
        harness.addMana(player2, ManaColor.BLACK, 3);
        harness.castInstant(player2, 0, angel.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(angel);
        resolveAllTriggers();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, false);
        }

        List<Permanent> servos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .toList();
        assertThat(servos).hasSize(2);
        assertThat(servos).allSatisfy(servo -> {
            assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
        });
    }
}
