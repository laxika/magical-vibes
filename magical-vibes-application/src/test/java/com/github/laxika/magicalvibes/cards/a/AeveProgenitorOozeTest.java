package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PredatorOoze;
import com.github.laxika.magicalvibes.cards.q.Quasiduplicate;
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

@CardUsed({AeveProgenitorOoze.class, PredatorOoze.class, GrizzlyBears.class, Quasiduplicate.class})
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

    @Test
    void entersWithoutCountersOrCopiesWhenNoOtherOozesOrEarlierSpellsExist() {
        castAeve();

        assertThat(findPermanents(player1, "Aeve, Progenitor Ooze")).hasSize(1);
        Permanent aeve = findPermanent(player1, "Aeve, Progenitor Ooze");
        assertThat(aeve.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(aeve.getCard().isToken()).isFalse();
    }

    @Test
    void stormCountsBothPlayersSpellsAndEachCopyCountsEarlierOozes() {
        gd.recordSpellCast(player1.getId(), new AeveProgenitorOoze());
        gd.recordSpellCast(player2.getId(), new AeveProgenitorOoze());

        castAeve();

        List<Permanent> aeves = findPermanents(player1, "Aeve, Progenitor Ooze");
        assertThat(aeves).hasSize(3);
        assertThat(aeves).filteredOn(p -> p.getCard().isToken())
                .extracting(p -> p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .containsExactlyInAnyOrder(0, 1);
        assertThat(aeves).filteredOn(p -> !p.getCard().isToken())
                .extracting(p -> p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .containsExactly(2);
    }

    @Test
    void tokenCreatedByAnotherCopyEffectIsNotLegendary() {
        harness.addToBattlefield(player1, new AeveProgenitorOoze());
        Permanent original = findPermanent(player1, "Aeve, Progenitor Ooze");
        harness.setHand(player1, List.of(new Quasiduplicate()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, original.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasEffectiveSupertype(gd, original, CardSupertype.LEGENDARY)).isTrue();
        List<Permanent> tokens = findPermanents(player1, "Aeve, Progenitor Ooze").stream()
                .filter(p -> p.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        assertThat(gqs.hasEffectiveSupertype(gd, tokens.getFirst(), CardSupertype.LEGENDARY)).isFalse();
        assertThat(tokens.getFirst().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void castAeve() {
        harness.castFromHand(player1, new AeveProgenitorOoze(), "{2}{G}{G}{G}");
        resolveAllTriggers();
    }
}
