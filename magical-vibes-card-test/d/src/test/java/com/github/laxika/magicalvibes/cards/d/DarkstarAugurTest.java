package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DarkstarAugur.class, Forest.class, GrizzlyBears.class})
class DarkstarAugurTest extends BaseCardTest {

    @Test
    void upkeepPutsTopCardIntoHandAndLosesItsManaValueInLife() {
        harness.addToBattlefield(player1, new DarkstarAugur());
        Card topCard = new GrizzlyBears();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void upkeepDoesNotLoseLifeForAZeroManaValueCard() {
        harness.addToBattlefield(player1, new DarkstarAugur());
        Card topCard = new Forest();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new DarkstarAugur()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @CardUsed(DarkstarAugur.class)
    void unpaidOffspringDoesNotCreateAToken() {
        harness.setHand(player1, List.of(new DarkstarAugur()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(DarkstarAugur.class)
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new DarkstarAugur());
        Card topCard = new DarkstarAugur();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(topCard));
        harness.setLife(player1, 20);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @CardUsed(DarkstarAugur.class)
    void emptyLibraryDoesNotCauseLifeLossOrADraw() {
        harness.addToBattlefield(player1, new DarkstarAugur());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed(DarkstarAugur.class)
    void offspringTokenAlsoRevealsACardAndLosesLifeOnUpkeep() {
        harness.setHand(player1, List.of(new DarkstarAugur()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.stack).isEmpty();
        Card firstCard = new DarkstarAugur();
        Card secondCard = new DarkstarAugur();
        harness.setLibrary(player1, List.of(firstCard, secondCard));
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 14);
        assertThat(gd.stack).isEmpty();
    }
}
