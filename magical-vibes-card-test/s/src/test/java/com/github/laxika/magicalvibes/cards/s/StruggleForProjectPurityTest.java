package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StruggleForProjectPurity.class, GrizzlyBears.class, Unsummon.class})
class StruggleForProjectPurityTest extends BaseCardTest {

    @Test
    @DisplayName("Brotherhood makes each opponent draw, then the controller draws for each card drawn")
    void brotherhoodDrawsForEachOpponentCardDrawn() {
        Card opponentCard = new GrizzlyBears();
        Card controllerCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(controllerCard));
        harness.setLibrary(player2, List.of(opponentCard));
        castCard("Brotherhood");

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(opponentCard);
        assertThat(gd.playerHands.get(player1.getId())).contains(controllerCard);
    }

    @Test
    @DisplayName("Enclave gives the attacking player twice the number of creatures attacking you in rad counters")
    void enclaveGivesTwiceTheNumberOfAttackersInRadCounters() {
        castCard("Enclave");
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0, 1));
        resolveAllTriggers();

        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Brotherhood does not trigger during an opponent's upkeep")
    void brotherhoodDoesNotDrawOnOpponentsUpkeep() {
        Card controllerCard = new GrizzlyBears();
        Card opponentCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(controllerCard));
        harness.setLibrary(player2, List.of(opponentCard));
        castCard("Brotherhood");

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(controllerCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(opponentCard);
    }

    @Test
    @DisplayName("Enclave has no Brotherhood upkeep ability")
    void enclaveDoesNotDrawOnControllersUpkeep() {
        Card controllerCard = new GrizzlyBears();
        Card opponentCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(controllerCard));
        harness.setLibrary(player2, List.of(opponentCard));
        castCard("Enclave");

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(controllerCard);
        assertThat(gd.playerHands.get(player2.getId())).doesNotContain(opponentCard);
    }

    @Test
    @DisplayName("Brotherhood has no Enclave attack ability")
    void brotherhoodDoesNotPutAnAttackAbilityOnTheStack() {
        castCard("Brotherhood");
        addCreatureReady(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerRadCounters.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Enclave does not trigger when its controller attacks an opponent")
    void enclaveDoesNotTriggerWhenYouAttack() {
        castCard("Enclave");
        addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(1)));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Removing one of two declared attackers does not reduce Enclave rad counters")
    void enclaveRemembersDeclaredAttackersAfterOneIsReturnedToHand() {
        castCard("Enclave");
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0, 1)));
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).contains(attacker.getCard());
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Removing the only declared attacker does not stop Enclave from resolving")
    void enclaveResolvesAfterTheOnlyAttackerIsReturnedToHand() {
        castCard("Enclave");
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        harness.castAndResolveInstant(player1, 0, attacker.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).contains(attacker.getCard());
        assertThat(gd.playerRadCounters.get(player2.getId())).isEqualTo(2);
    }

    private void castCard(String mode) {
        harness.castFromHand(player1, new StruggleForProjectPurity(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, mode);
    }
}
