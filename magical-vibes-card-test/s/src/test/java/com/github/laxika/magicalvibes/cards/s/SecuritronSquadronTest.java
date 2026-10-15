package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SecuritronSquadron.class)
class SecuritronSquadronTest extends BaseCardTest {

    @Test
    @DisplayName("Squad creates one token copy for each additional payment")
    void squadCreatesTokenCopies() {
        harness.setHand(player1, List.of(new SecuritronSquadron()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{3}", "{3}"));
        resolveAllTriggers();

        List<Permanent> securitrons = findPermanents(player1, "Securitron Squadron");
        assertThat(securitrons).hasSize(3);
        assertThat(securitrons).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    @DisplayName("A squad token receives counters from itself and the original")
    void tokensEnterWithCounters() {
        harness.setHand(player1, List.of(new SecuritronSquadron()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{3}"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Securitron Squadron"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .allSatisfy(token -> assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
    }

    @Test
    @DisplayName("Squad copies see every token in their simultaneous entry")
    void simultaneousSquadCopiesEachReceiveThreeCounters() {
        harness.setHand(player1, List.of(new SecuritronSquadron()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{3}", "{3}"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Securitron Squadron"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(token -> assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3));
        assertThat(findPermanents(player1, "Securitron Squadron"))
                .filteredOn(permanent -> !permanent.getCard().isToken())
                .allSatisfy(original -> assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @DisplayName("Casting without squad creates no tokens or counters")
    void noSquadPaymentCreatesNoTokens() {
        harness.setHand(player1, List.of(new SecuritronSquadron()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Securitron Squadron"))
                .hasSize(1)
                .allSatisfy(original -> assertThat(original.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }

    @Test
    @DisplayName("Counters are added by triggers after the squad token enters")
    void countersWaitForTriggeredAbilitiesToResolve() {
        harness.setHand(player1, List.of(new SecuritronSquadron()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{3}"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Securitron Squadron"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1)
                .allSatisfy(token -> assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());

        resolveAllTriggers();

        assertThat(findPermanents(player1, "Securitron Squadron"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .allSatisfy(token -> assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
    }

    @Test
    @DisplayName("An opposing Squadron does not add counters to your tokens")
    void opposingSquadronDoesNotTriggerForYourTokens() {
        harness.addToBattlefield(player2, new SecuritronSquadron());
        harness.setHand(player1, List.of(new SecuritronSquadron()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{3}"));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Securitron Squadron"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(1)
                .allSatisfy(token -> assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
        assertThat(findPermanents(player2, "Securitron Squadron"))
                .hasSize(1)
                .allSatisfy(opponent -> assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }
}
