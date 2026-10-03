package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.u.UnlicensedDisintegration;
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

@CardUsed({AmbitiousAetherborn.class, UnlicensedDisintegration.class})
class AmbitiousAetherbornTest extends BaseCardTest {

    @Test
    @DisplayName("Fabricate mode puts a +1/+1 counter on Ambitious Aetherborn")
    void fabricateCountersMode() {
        castAetherborn(0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        Permanent aetherborn = findPermanent(player1, "Ambitious Aetherborn");
        assertThat(aetherborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, aetherborn)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, aetherborn)).isEqualTo(4);
    }

    @Test
    @DisplayName("Fabricate mode creates a 1/1 colorless Servo artifact creature token")
    void fabricateServoMode() {
        castAetherborn(1);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

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

    @Test
    @DisplayName("Fabricate asks for its choice only when the triggered ability resolves")
    void fabricateChoiceIsMadeOnResolution() {
        castAetherborn(0);
        harness.passBothPriorities();

        Permanent aetherborn = findPermanent(player1, "Ambitious Aetherborn");
        assertThat(aetherborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(aetherborn.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Fabricate creates a Servo if its source is destroyed before resolution")
    void fabricateCreatesServoWhenSourceIsGone() {
        castAetherborn(0);
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new UnlicensedDisintegration()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Ambitious Aetherborn"));
        harness.assertInGraveyard(player1, "Ambitious Aetherborn");

        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }
    private void castAetherborn(int mode) {
        harness.setHand(player1, List.of(new AmbitiousAetherborn()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
    }

}
