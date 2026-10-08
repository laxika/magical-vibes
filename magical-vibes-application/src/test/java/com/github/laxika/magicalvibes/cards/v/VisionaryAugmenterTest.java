package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ChandrasPyrohelix;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VisionaryAugmenter.class, ChandrasPyrohelix.class})
class VisionaryAugmenterTest extends BaseCardTest {

    @Test
    @DisplayName("Fabricate mode puts two +1/+1 counters on Visionary Augmenter")
    void fabricateCountersMode() {
        castAugmenter(0);
        resolveAllTriggers();

        Permanent augmenter = findPermanent(player1, "Visionary Augmenter");
        assertThat(augmenter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, augmenter)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, augmenter)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fabricate mode creates two 1/1 colorless Servo artifact creature tokens")
    void fabricateServoMode() {
        castAugmenter(1);
        resolveAllTriggers();

        List<Permanent> servos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .toList();

        assertThat(servos).hasSize(2);
        assertThat(servos).allSatisfy(servo -> {
            assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
            assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
            assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
            assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Fabricate offers its choice when the triggered ability resolves")
    void fabricateChoiceIsMadeOnResolution() {
        harness.setHand(player1, List.of(new VisionaryAugmenter()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent augmenter = findPermanent(player1, "Visionary Augmenter");
        assertThat(augmenter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(augmenter.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Fabricate creates Servos if its source dies before the trigger resolves")
    void fabricateCreatesServosWhenSourceDies() {
        castAugmenter(0);
        harness.passBothPriorities();
        Permanent augmenter = findPermanent(player1, "Visionary Augmenter");
        harness.setHand(player2, List.of(new ChandrasPyrohelix()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castInstant(player2, 0, Map.of(augmenter.getId(), 2));
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Visionary Augmenter");

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO)))
                .hasSize(2);
    }

    private void castAugmenter(int mode) {
        harness.setHand(player1, List.of(new VisionaryAugmenter()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0, mode);
    }

}
