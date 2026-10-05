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

@CardUsed({PeemaOutrider.class, TidyConclusion.class})
class PeemaOutriderTest extends BaseCardTest {

    @Test
    @DisplayName("Fabricate mode puts a +1/+1 counter on Peema Outrider")
    void fabricateCountersMode() {
        castOutrider(0);
        resolveAllTriggers();

        Permanent outrider = findPermanent(player1, "Peema Outrider");
        assertThat(outrider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Fabricate mode creates a 1/1 colorless Servo artifact creature token")
    void fabricateServoMode() {
        castOutrider(1);
        resolveAllTriggers();

        List<Permanent> servos = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO))
                .toList();

        assertThat(servos).hasSize(1);
        Permanent servo = servos.getFirst();
        assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(servo.getCard().getColor()).isNull();
        assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
    }

    private void castOutrider(int mode) {
        harness.setHand(player1, List.of(new PeemaOutrider()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.castCreature(player1, 0, mode);
    }

    @Test
    @DisplayName("Fabricate creates a Servo when Outrider dies before its trigger resolves")
    void fabricateCreatesServoWhenSourceDies() {
        castOutrider(0);
        harness.passBothPriorities();

        Permanent outrider = findPermanent(player1, "Peema Outrider");
        harness.setHand(player2, List.of(new TidyConclusion()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castAndResolveInstant(player2, 0, outrider.getId());
        harness.assertInGraveyard(player1, "Peema Outrider");

        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getSubtypes().contains(CardSubtype.SERVO)))
                .hasSize(1);
    }
}
