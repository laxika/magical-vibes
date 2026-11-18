package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.PermanentGrants;
import com.github.laxika.magicalvibes.cards.s.SkyshroudArcher;
import com.github.laxika.magicalvibes.cards.s.SkyshroudFalcon;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CrossbowAmbush.class, SkyshroudArcher.class, SkyshroudFalcon.class})
class CrossbowAmbushTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Crossbow Ambush grants reach to existing creatures you control")
    void grantsReachToOwnCreatures() {
        Permanent ownCreature = addCreatureReady(player1, new SkyshroudArcher());
        Permanent opponentCreature = addCreatureReady(player2, new SkyshroudArcher());

        harness.castFromHand(player1, new CrossbowAmbush(), "{G}");
        harness.passBothPriorities();

        Permanent creatureEnteringLater = addCreatureReady(player1, new SkyshroudArcher());

        assertThat(PermanentGrants.keywordsOf(gd, ownCreature)).contains(Keyword.REACH);
        assertThat(PermanentGrants.keywordsOf(gd, opponentCreature)).doesNotContain(Keyword.REACH);
        assertThat(PermanentGrants.keywordsOf(gd, creatureEnteringLater)).doesNotContain(Keyword.REACH);
    }

    @Test
    @DisplayName("Reach granted by Crossbow Ambush expires at end of turn")
    void reachExpiresAtEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new SkyshroudArcher());

        harness.castFromHand(player1, new CrossbowAmbush(), "{G}");
        harness.passBothPriorities();
        assertThat(PermanentGrants.keywordsOf(gd, ownCreature)).contains(Keyword.REACH);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(PermanentGrants.keywordsOf(gd, ownCreature)).doesNotContain(Keyword.REACH);
    }

    @Test
    @DisplayName("Reach granted by Crossbow Ambush lets a creature block a creature with flying")
    void reachAllowsBlockingFlyingCreature() {
        Permanent blocker = addCreatureReady(player2, new SkyshroudArcher());
        addCreatureReady(player1, new SkyshroudFalcon());

        harness.castFromHand(player2, new CrossbowAmbush(), "{G}");
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Crossbow Ambush affects all creatures controlled when it resolves")
    void includesCreaturesEnteringBeforeResolution() {
        Permanent firstCreature = addCreatureReady(player1, new SkyshroudArcher());

        harness.castFromHand(player1, new CrossbowAmbush(), "{G}");
        Permanent secondCreature = addCreatureReady(player1, new SkyshroudArcher());
        assertThat(PermanentGrants.keywordsOf(gd, firstCreature)).doesNotContain(Keyword.REACH);
        assertThat(PermanentGrants.keywordsOf(gd, secondCreature)).doesNotContain(Keyword.REACH);

        harness.passBothPriorities();

        assertThat(PermanentGrants.keywordsOf(gd, firstCreature)).contains(Keyword.REACH);
        assertThat(PermanentGrants.keywordsOf(gd, secondCreature)).contains(Keyword.REACH);
    }

    @Test
    @DisplayName("Crossbow Ambush resolves without creatures and does not affect later arrivals")
    void resolvesWithNoCreatures() {
        harness.castFromHand(player1, new CrossbowAmbush(), "{G}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Crossbow Ambush");
        assertThat(gd.stack).isEmpty();

        Permanent laterCreature = addCreatureReady(player1, new SkyshroudArcher());
        assertThat(PermanentGrants.keywordsOf(gd, laterCreature)).doesNotContain(Keyword.REACH);
    }
}
