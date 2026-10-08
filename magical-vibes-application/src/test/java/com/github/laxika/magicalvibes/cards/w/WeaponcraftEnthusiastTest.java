package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.h.HarnessedLightning;
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

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeaponcraftEnthusiast.class, HarnessedLightning.class})
class WeaponcraftEnthusiastTest extends BaseCardTest {

    @Test
    @DisplayName("Fabricate mode puts two +1/+1 counters on Weaponcraft Enthusiast")
    void fabricateCountersMode() {
        castEnthusiast(0);
        resolveCreatureAndEtb();

        Permanent enthusiast = findPermanent(player1, "Weaponcraft Enthusiast");
        assertThat(enthusiast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, enthusiast)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enthusiast)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fabricate mode creates two 1/1 colorless Servo artifact creature tokens")
    void fabricateServoMode() {
        castEnthusiast(1);
        resolveCreatureAndEtb();

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
    @DisplayName("Fabricate offers its choice when the trigger resolves")
    void choiceIsMadeAtResolution() {
        harness.setHand(player1, List.of(new WeaponcraftEnthusiast()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent enthusiast = findPermanent(player1, "Weaponcraft Enthusiast");
        assertThat(enthusiast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(enthusiast.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Fabricate creates two Servos if the creature dies before the trigger resolves")
    void createsServosWhenSourceDiesBeforeResolution() {
        castEnthusiast(0);
        harness.passBothPriorities();
        Permanent enthusiast = findPermanent(player1, "Weaponcraft Enthusiast");

        harness.setHand(player2, List.of(new HarnessedLightning()));
        harness.addMana(player2, ManaColor.RED, 2);
        harness.castAndResolveInstant(player2, 0, enthusiast.getId());
        harness.handleXValueChosen(player2, 1);
        harness.assertInGraveyard(player1, "Weaponcraft Enthusiast");

        harness.passBothPriorities();

        List<Permanent> servos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .toList();
        assertThat(servos).hasSize(2);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    private void castEnthusiast(int mode) {
        harness.setHand(player1, List.of(new WeaponcraftEnthusiast()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0, mode);
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
