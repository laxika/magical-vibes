package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StingingCaveCrawler.class, Forest.class, GrizzlyBears.class, Shock.class, Spellbook.class})
class StingingCaveCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with four permanent cards in the graveyard draws a card and loses 1 life")
    void thresholdDrawsAndLosesLife() {
        addCreatureReady(player1, new StingingCaveCrawler());
        harness.setGraveyard(player1, List.of(
                new Forest(), new GrizzlyBears(), new Spellbook(), new StingingCaveCrawler(), new Shock()));
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Fewer than four permanent cards or an opponent's graveyard does not trigger")
    void thresholdRequiresFourOwnPermanentCards() {
        addCreatureReady(player1, new StingingCaveCrawler());
        harness.setGraveyard(player1, List.of(new Forest(), new GrizzlyBears(), new Spellbook(), new Shock()));
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("The graveyard threshold is checked again when the trigger resolves")
    void thresholdMustStillBeMetOnResolution() {
        addCreatureReady(player1, new StingingCaveCrawler());
        harness.setGraveyard(player1, List.of(
                new Forest(), new GrizzlyBears(), new Spellbook(), new StingingCaveCrawler()));
        harness.setLibrary(player1, List.of(new Forest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        declareAttackers(player1, List.of(0));
        harness.setGraveyard(player1, List.of(new Forest(), new GrizzlyBears(), new Spellbook()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Four copies of the same permanent card satisfy descend four")
    void repeatedPermanentCardsCountSeparately() {
        addCreatureReady(player1, new StingingCaveCrawler());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new StingingCaveCrawler()));
        harness.setLife(player1, 20);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        harness.assertInHand(player1, "Stinging Cave Crawler");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 19);
    }

    @Test
    @DisplayName("Reaching descend four after attacking does not create a trigger")
    void thresholdReachedAfterAttackDoesNotTrigger() {
        addCreatureReady(player1, new StingingCaveCrawler());
        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLife(player1, 20);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).isEmpty();
        harness.setGraveyard(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deathtouch destroys a blocker that survives one damage without it")
    void deathtouchDestroysBlocker() {
        addCreatureReady(player1, new StingingCaveCrawler());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Stinging Cave Crawler");
    }
}
