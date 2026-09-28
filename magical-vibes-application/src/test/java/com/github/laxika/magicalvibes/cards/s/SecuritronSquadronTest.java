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
        harness.passBothPriorities();
        resolveAllTriggers();

        List<Permanent> securitrons = findPermanents(player1, "Securitron Squadron");
        assertThat(securitrons).hasSize(3);
        assertThat(securitrons).filteredOn(permanent -> permanent.getCard().isToken()).hasSize(2);
    }

    @Test
    @DisplayName("Each token enters with a +1/+1 counter")
    void tokensEnterWithCounters() {
        harness.setHand(player1, List.of(new SecuritronSquadron()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreatureWithRepeatedCosts(player1, 0, List.of("{3}"));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Securitron Squadron"))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .allSatisfy(token -> assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2));
    }
}
