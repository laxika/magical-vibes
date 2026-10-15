package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FinalDeath;
import com.github.laxika.magicalvibes.cards.f.FuriousRise;
import com.github.laxika.magicalvibes.cards.m.MossViper;
import com.github.laxika.magicalvibes.cards.n.NyleasForerunner;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SetessanChampion.class, NyleasForerunner.class, MossViper.class, Forest.class, FinalDeath.class, FuriousRise.class})
class SetessanChampionTest extends BaseCardTest {

    @Test
    @DisplayName("An enchantment entering under your control puts a counter on Setessan Champion and draws a card")
    void allyEnchantmentEntryPutsCounterAndDrawsCard() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new SetessanChampion());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new FuriousRise()));
        harness.addMana(player1, ManaColor.RED, 3);

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
        harness.setHand(player1, List.of(new MossViper()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("An opponent's enchantment entering does not trigger Setessan Champion")
    void opponentEnchantmentEntryDoesNotTrigger() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new SetessanChampion());
        harness.setHand(player2, List.of(new FuriousRise()));
        harness.addMana(player2, ManaColor.RED, 3);
        harness.forceActivePlayer(player2);

        harness.castEnchantment(player2, 0);
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Each enchantment entry gives another counter and another card")
    void repeatedEnchantmentEntriesEachTrigger() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new SetessanChampion());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.setHand(player1, List.of(new NyleasForerunner(), new NyleasForerunner()));
        harness.addMana(player1, ManaColor.GREEN, 10);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The controller still draws if Champion is exiled before its trigger resolves")
    void drawsAfterChampionLeavesBattlefield() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new SetessanChampion());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setHand(player1, List.of(new NyleasForerunner()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.setHand(player2, List.of(new FinalDeath()));
        harness.addMana(player2, ManaColor.BLACK, 5);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player2, 0, champion.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Setessan Champion");
        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }
}
