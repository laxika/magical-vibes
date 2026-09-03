package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HonorOfThePure;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EnchantresssPresence.class, Forest.class, GrizzlyBears.class, HonorOfThePure.class})
class EnchantresssPresenceTest extends BaseCardTest {

    @Test
    @DisplayName("Casting an enchantment spell draws a card")
    void enchantmentCastDrawsCard() {
        harness.addToBattlefield(player1, new EnchantresssPresence());
        harness.setHand(player1, List.of(new HonorOfThePure()));
        Forest drawn = new Forest();
        setDeck(List.of(drawn));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(drawn);
    }

    @Test
    @DisplayName("Casting a non-enchantment spell does not draw a card")
    void nonEnchantmentCastDoesNotDrawCard() {
        harness.addToBattlefield(player1, new EnchantresssPresence());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        Forest notDrawn = new Forest();
        setDeck(List.of(notDrawn));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(notDrawn);
        assertThat(gd.playerDecks.get(player1.getId())).contains(notDrawn);
    }

    @Test
    @DisplayName("An opponent casting an enchantment does not draw a card for you")
    void opponentEnchantmentCastDoesNotDrawCard() {
        harness.addToBattlefield(player1, new EnchantresssPresence());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new HonorOfThePure()));
        Forest notDrawn = new Forest();
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(notDrawn);
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.castEnchantment(player2, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(notDrawn);
        assertThat(gd.playerDecks.get(player1.getId())).contains(notDrawn);
    }

    private void setDeck(List<Forest> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
