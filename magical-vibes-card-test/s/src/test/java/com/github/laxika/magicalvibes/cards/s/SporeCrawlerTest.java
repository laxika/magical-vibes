package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FlourishingHunter;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SporeCrawler.class, FlourishingHunter.class, Forest.class, WrathOfGod.class})
class SporeCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Spore Crawler dies from Wrath of God, draws a card")
    void diesFromWrathOfGodDrawsCard() {
        harness.addToBattlefield(player1, new SporeCrawler());
        harness.addToBattlefield(player2, new FlourishingHunter());

        harness.setLibrary(player1, List.of(new Forest()));

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities(); // resolve Wrath

        harness.assertInGraveyard(player1, "Spore Crawler");
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Spore Crawler"));

        harness.passBothPriorities(); // resolve death trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Spore Crawler dies as blocker to a bigger attacker, draws a card")
    void diesInCombatAsBlockerDrawsCard() {
        Permanent crawlerPerm = addCreatureReady(player1, new SporeCrawler());
        crawlerPerm.setBlocking(true);
        crawlerPerm.addBlockingTarget(0);
        Permanent attacker = addCreatureReady(player2, new FlourishingHunter());
        attacker.setAttacking(true);

        harness.setLibrary(player1, List.of(new Forest()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // combat damage

        harness.assertInGraveyard(player1, "Spore Crawler");
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Spore Crawler"));

        harness.passBothPriorities(); // resolve death trigger

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore + 1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Each Spore Crawler dying simultaneously draws exactly one card")
    void simultaneousDeathsEachDrawOneCard() {
        harness.addToBattlefield(player1, new SporeCrawler());
        harness.addToBattlefield(player1, new SporeCrawler());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();

        harness.castFromHand(player1, new WrathOfGod(), "{2}{W}{W}");
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2).allMatch(e ->
                e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && e.getCard().getName().equals("Spore Crawler")
                        && e.getControllerId().equals(player1.getId()));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();

        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize);
        assertThat(gd.stack).isEmpty();
    }
}
