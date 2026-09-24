package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PredatorOoze;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AeveProgenitorOoze.class, PredatorOoze.class, GrizzlyBears.class})
class AeveProgenitorOozeTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a +1/+1 counter for each other Ooze you control")
    void entersWithCountersForOtherOozes() {
        harness.addToBattlefield(player1, new PredatorOoze());
        harness.addToBattlefield(player1, new PredatorOoze());
        harness.addToBattlefield(player2, new PredatorOoze());

        castAeve();

        Permanent aeve = findPermanent(player1, "Aeve, Progenitor Ooze");
        assertThat(aeve.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Storm creates nonlegendary Ooze token copies with current Ooze-count counters")
    void stormCreatesNonlegendaryTokenCopyWithCounters() {
        harness.addToBattlefield(player1, new PredatorOoze());
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());

        castAeve();

        List<Permanent> aeves = findPermanents(player1, "Aeve, Progenitor Ooze");
        assertThat(aeves).hasSize(2);
        Permanent token = aeves.stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(token.getCard().getSupertypes()).doesNotContain(CardSupertype.LEGENDARY);
    }

    private void castAeve() {
        harness.setHand(player1, List.of(new AeveProgenitorOoze()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
