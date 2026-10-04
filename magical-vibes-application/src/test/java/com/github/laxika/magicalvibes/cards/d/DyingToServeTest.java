package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DyingToServe.class, DangerousWager.class, GrizzlyBears.class, Peek.class})
class DyingToServeTest extends BaseCardTest {

    @Test
    @DisplayName("Creates one tapped 2/2 black Zombie after discarding multiple cards")
    void createsTappedZombieAfterDiscardingMultipleCards() {
        harness.addToBattlefield(player1, new DyingToServe());
        harness.setHand(player1, List.of(new DangerousWager(), new GrizzlyBears(), new Peek()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        Permanent zombie = findPermanent(player1, "Zombie");
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
        assertThat(zombie.isTapped()).isTrue();
        assertThat(zombie.getCard().getPower()).isEqualTo(2);
        assertThat(zombie.getCard().getToughness()).isEqualTo(2);
        assertThat(zombie.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(zombie.getCard().getSubtypes()).contains(CardSubtype.ZOMBIE);
    }

    @Test
    @DisplayName("Triggers only once each turn")
    void triggersOnlyOnceEachTurn() {
        harness.addToBattlefield(player1, new DyingToServe());
        harness.setHand(player1, List.of(new DangerousWager(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(
                new DangerousWager(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);

        harness.castAndResolveInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    @DisplayName("Discarding an empty hand does not consume the trigger")
    void emptyHandDoesNotConsumeTrigger() {
        harness.addToBattlefield(player1, new DyingToServe());
        harness.setHand(player1, List.of(new DangerousWager()));
        harness.setLibrary(player1, List.of(
                new DangerousWager(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0);
        assertThat(countPermanents(player1, "Zombie")).isZero();

        harness.castAndResolveInstant(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);
    }

    @Test
    @DisplayName("An opponent's discard does not trigger the ability")
    void opponentDiscardDoesNotTrigger() {
        harness.addToBattlefield(player1, new DyingToServe());
        harness.setHand(player2, List.of(new DangerousWager(), new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 2);

        harness.castAndResolveInstant(player2, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Zombie")).isZero();
        assertThat(countPermanents(player2, "Zombie")).isZero();
    }

    @Test
    @DisplayName("Each copy triggers independently once per turn")
    void copiesTriggerIndependently() {
        harness.addToBattlefield(player1, new DyingToServe());
        harness.addToBattlefield(player1, new DyingToServe());
        harness.setHand(player1, List.of(new DangerousWager(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(
                new DangerousWager(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);

        harness.castAndResolveInstant(player1, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
    }

    @Test
    @DisplayName("The ability can trigger again during the opponent's next turn")
    void triggersAgainOnOpponentsTurn() {
        harness.addToBattlefield(player1, new DyingToServe());
        harness.setHand(player1, List.of(new DangerousWager(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0);
        resolveAllTriggers();
        assertThat(countPermanents(player1, "Zombie")).isEqualTo(1);

        harness.passUntil(player2, TurnStep.UPKEEP);
        harness.setHand(player1, List.of(new DangerousWager(), new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(2);
        assertThat(countPermanents(player2, "Zombie")).isZero();
    }
}
