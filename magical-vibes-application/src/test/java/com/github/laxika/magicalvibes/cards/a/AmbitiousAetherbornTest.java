package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AmbitiousAetherbornTest extends BaseCardTest {

    @Test
    @DisplayName("Fabricate mode puts a +1/+1 counter on Ambitious Aetherborn")
    void fabricateCountersMode() {
        castAetherborn(0);
        resolveCreatureAndEtb();

        Permanent aetherborn = findPermanent(player1, "Ambitious Aetherborn");
        assertThat(aetherborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, aetherborn)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, aetherborn)).isEqualTo(4);
    }

    @Test
    @DisplayName("Fabricate mode creates a 1/1 colorless Servo artifact creature token")
    void fabricateServoMode() {
        castAetherborn(1);
        resolveCreatureAndEtb();

        List<Permanent> servos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .toList();

        assertThat(servos).hasSize(1);
        Permanent servo = servos.getFirst();
        assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
    }

    private void castAetherborn(int mode) {
        harness.setHand(player1, List.of(new AmbitiousAetherborn()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0, mode);
    }

    private void resolveCreatureAndEtb() {
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
