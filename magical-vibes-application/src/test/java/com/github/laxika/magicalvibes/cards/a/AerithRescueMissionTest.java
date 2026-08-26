package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AerithRescueMission.class, GrizzlyBears.class})
class AerithRescueMissionTest extends BaseCardTest {

    @Test
    @DisplayName("Take the Elevator creates three Hero tokens")
    void takeTheElevatorCreatesHeroTokens() {
        harness.setHand(player1, List.of(new AerithRescueMission()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();

        List<Permanent> heroes = findPermanents(player1, "Hero");
        assertThat(heroes).hasSize(3);
        assertThat(heroes).allSatisfy(hero -> {
            assertThat(hero.getCard().isToken()).isTrue();
            assertThat(hero.getCard().getPower()).isEqualTo(1);
            assertThat(hero.getCard().getToughness()).isEqualTo(1);
            assertThat(hero.getCard().getSubtypes()).contains(CardSubtype.HERO);
        });
    }

    @Test
    @DisplayName("Take 59 Flights of Stairs taps targets and stuns one chosen at resolution")
    void takeFlightsOfStairsTapsAndStunsOneTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AerithRescueMission()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castModalSorcery(player1, 0, 1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(first.isTapped()).isTrue();
        assertThat(second.isTapped()).isTrue();
        assertThat(third.isTapped()).isTrue();
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());

        harness.handlePermanentChosen(player1, second.getId());

        assertThat(first.getCounterCount(CounterType.STUN)).isZero();
        assertThat(second.getCounterCount(CounterType.STUN)).isEqualTo(1);
        assertThat(third.getCounterCount(CounterType.STUN)).isZero();
    }
}
