package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PrismariApprentice.class, GiantGrowth.class, GrizzlyBears.class})
class PrismariApprenticeTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an instant makes Prismari Apprentice unblockable without adding a counter")
    void castingLowManaInstantMakesApprenticeUnblockable() {
        Permanent apprentice = addCreatureReady(player1, new PrismariApprentice());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(apprentice.isCantBeBlocked()).isTrue();
        assertThat(apprentice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting an instant or sorcery with mana value 5 or greater adds a counter")
    void castingHighManaValueSpellAddsCounter() {
        Permanent apprentice = addCreatureReady(player1, new PrismariApprentice());
        harness.setHand(player1, List.of(highManaValueSorcery(false)));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castSorcery(player1, 0, 0);
        resolveAllTriggers();

        assertThat(apprentice.isCantBeBlocked()).isTrue();
        assertThat(apprentice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Copying a high-mana-value spell triggers both Prismari Apprentice abilities")
    void copyingHighManaValueSpellAddsAnotherCounter() {
        Permanent apprentice = addCreatureReady(player1, new PrismariApprentice());
        Permanent conspireA = addCreatureReady(player1, new GrizzlyBears());
        Permanent conspireB = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(highManaValueSorcery(true)));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castWithConspire(player1, 0, null, List.of(conspireA.getId(), conspireB.getId()));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(apprentice.isCantBeBlocked()).isTrue();
        assertThat(apprentice.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prismari Apprentice's unblockable effect wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        Permanent apprentice = addCreatureReady(player1, new PrismariApprentice());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(apprentice.isCantBeBlocked()).isFalse();
    }

    private Card highManaValueSorcery(boolean conspire) {
        Card card = new Card();
        card.setName("High Mana Value Sorcery");
        card.setType(CardType.SORCERY);
        card.setManaCost("{5}");
        card.setColor(CardColor.GREEN);
        card.setColors(List.of(CardColor.GREEN));
        if (conspire) {
            card.setKeywords(Set.of(Keyword.CONSPIRE));
        }
        return card;
    }
}
