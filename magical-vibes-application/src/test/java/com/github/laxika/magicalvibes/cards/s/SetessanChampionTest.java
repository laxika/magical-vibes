package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SetessanChampion.class, GloriousAnthem.class, GrizzlyBears.class, Forest.class})
class SetessanChampionTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under your control puts a counter on Setessan Champion and draws a card")
    void allyEnchantmentEntryPutsCounterAndDrawsCard() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new SetessanChampion());
        setLibrary(player1, new Forest());
        harness.setHand(player1, List.of(new GloriousAnthem()));
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("A non-enchantment entering under your control does not trigger Setessan Champion")
    void nonEnchantmentEntryDoesNotTrigger() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new SetessanChampion());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's enchantment entering does not trigger Setessan Champion")
    void opponentEnchantmentEntryDoesNotTrigger() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new SetessanChampion());
        harness.setHand(player2, List.of(new GloriousAnthem()));
        harness.addMana(player2, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void setLibrary(com.github.laxika.magicalvibes.model.Player player, Card card) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).add(card);
    }
}
