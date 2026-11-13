package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.t.TidyConclusion;
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

@CardUsed({PropellerPioneer.class, TidyConclusion.class})
class PropellerPioneerTest extends BaseCardTest {

    private static final String COUNTER_MODE = "Put a +1/+1 counter on Propeller Pioneer";
    private static final String SERVO_MODE = "Create a 1/1 colorless Servo artifact creature token";

    @Test
    @DisplayName("Fabricate mode puts a +1/+1 counter on Propeller Pioneer")
    void fabricateCountersMode() {
        castPioneer();
        harness.passBothPriorities();
        harness.handleListChoice(player1, COUNTER_MODE);

        Permanent pioneer = findPermanent(player1, "Propeller Pioneer");
        assertThat(pioneer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, pioneer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, pioneer)).isEqualTo(2);
    }

    @Test
    @DisplayName("Fabricate mode creates a 1/1 colorless Servo artifact creature token")
    void fabricateServoMode() {
        castPioneer();
        harness.passBothPriorities();
        harness.handleListChoice(player1, SERVO_MODE);

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
    @DisplayName("Fabricate creates a Servo if Pioneer dies before its trigger resolves")
    void fabricateCreatesServoWhenSourceDies() {
        castPioneer();

        Permanent pioneer = findPermanent(player1, "Propeller Pioneer");
        harness.setHand(player2, List.of(new TidyConclusion()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player2, 0, pioneer.getId());
        harness.assertInGraveyard(player1, "Propeller Pioneer");

        harness.passBothPriorities();
        harness.handleListChoice(player1, COUNTER_MODE);
        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
    }

    private void castPioneer() {
        harness.setHand(player1, List.of(new PropellerPioneer()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

}
