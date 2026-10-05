package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.t.TidyConclusion;
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

@CardUsed({IronLeagueSteed.class, TidyConclusion.class})
class IronLeagueSteedTest extends BaseCardTest {

    @Test
    @DisplayName("Fabricate mode puts a +1/+1 counter on Iron League Steed")
    void fabricateCountersMode() {
        castSteed(0);

        Permanent steed = findPermanent(player1, "Iron League Steed");
        assertThat(steed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, steed)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, steed)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fabricate mode creates a 1/1 colorless Servo artifact creature token")
    void fabricateServoMode() {
        castSteed(1);

        List<Permanent> servos = findPermanents(player1, "Servo");

        assertThat(servos).hasSize(1);
        Permanent servo = servos.getFirst();
        assertThat(servo.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(servo.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.getEffectivePower(gd, servo)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, servo)).isEqualTo(1);
        assertThat(servo.getCard().getColor()).isNull();
        assertThat(findPermanent(player1, "Iron League Steed")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Fabricate choice is made when the triggered ability resolves")
    void fabricateChoiceAtResolution() {
        harness.setHand(player1, List.of(new IronLeagueSteed()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent steed = findPermanent(player1, "Iron League Steed");
        assertThat(steed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Servo")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();

        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        assertThat(steed.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countPermanents(player1, "Servo")).isZero();
    }

    @Test
    @DisplayName("Fabricate creates a Servo if the Steed leaves before resolution")
    void fabricateCreatesServoWhenSourceLeaves() {
        harness.setHand(player1, List.of(new IronLeagueSteed()));
        harness.setHand(player2, List.of(new TidyConclusion()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.addMana(player2, ManaColor.BLACK, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent steed = findPermanent(player1, "Iron League Steed");
        harness.castAndResolveInstant(player2, 0, steed.getId());
        harness.assertInGraveyard(player1, "Iron League Steed");

        resolveAllTriggers();

        assertThat(countPermanents(player1, "Servo")).isEqualTo(1);
        assertThat(countPermanents(player2, "Servo")).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void castSteed(int mode) {
        harness.setHand(player1, List.of(new IronLeagueSteed()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, mode == 0);
        resolveAllTriggers();
    }
}
