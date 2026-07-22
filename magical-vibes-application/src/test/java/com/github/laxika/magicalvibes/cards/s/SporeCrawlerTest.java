package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SporeCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Spore Crawler dies from Wrath of God, draws a card")
    void diesFromWrathOfGodDrawsCard() {
        harness.addToBattlefield(player1, new SporeCrawler());
        harness.addToBattlefield(player2, new GrizzlyBears());

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());

        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities(); // resolve Wrath

        harness.assertInGraveyard(player1, "Spore Crawler");
        assertThat(gd.stack).anyMatch(e -> e.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && e.getCard().getName().equals("Spore Crawler"));

        harness.passBothPriorities(); // resolve death trigger

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handSizeBefore - 1 + 1);
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Forest"));
    }

    @Test
    @DisplayName("Spore Crawler dies as blocker to a bigger attacker, draws a card")
    void diesInCombatAsBlockerDrawsCard() {
        SporeCrawler crawler = new SporeCrawler();
        Permanent crawlerPerm = new Permanent(crawler);
        crawlerPerm.setSummoningSick(false);
        crawlerPerm.setBlocking(true);
        crawlerPerm.addBlockingTarget(0);
        gd.playerBattlefields.get(player1.getId()).add(crawlerPerm);

        GrizzlyBears big = new GrizzlyBears();
        big.setPower(5);
        big.setToughness(5);
        Permanent attacker = new Permanent(big);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).add(new Forest());
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
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getName().equals("Forest"));
    }
}
