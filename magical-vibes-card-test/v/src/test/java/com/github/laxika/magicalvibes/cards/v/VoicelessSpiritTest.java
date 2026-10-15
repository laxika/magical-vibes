package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.ChapelGeist;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoicelessSpirit.class, ChapelGeist.class, WalkingCorpse.class})
class VoicelessSpiritTest extends BaseCardTest {

    @Test
    void nonFlyingCreatureCannotBlock() {
        addCreatureReady(player1, new VoicelessSpirit());
        addCreatureReady(player2, new WalkingCorpse());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    void firstStrikeKillsGroundAttackerBeforeItDealsDamage() {
        addCreatureReady(player1, new WalkingCorpse());
        addCreatureReady(player2, new VoicelessSpirit());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Walking Corpse");
        harness.assertNotOnBattlefield(player1, "Walking Corpse");
        harness.assertOnBattlefield(player2, "Voiceless Spirit");
        harness.assertLife(player2, 20);
    }

    @Test
    void flyingBlockerSurvivingFirstStrikeDealsRegularDamage() {
        addCreatureReady(player1, new VoicelessSpirit());
        addCreatureReady(player2, new ChapelGeist());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Voiceless Spirit");
        harness.assertNotOnBattlefield(player1, "Voiceless Spirit");
        harness.assertOnBattlefield(player2, "Chapel Geist");
        harness.assertLife(player2, 20);
    }

    @Test
    void opposingFirstStrikersDealDamageSimultaneously() {
        addCreatureReady(player1, new VoicelessSpirit());
        addCreatureReady(player2, new VoicelessSpirit());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player1, "Voiceless Spirit");
        harness.assertInGraveyard(player2, "Voiceless Spirit");
        harness.assertNotOnBattlefield(player1, "Voiceless Spirit");
        harness.assertNotOnBattlefield(player2, "Voiceless Spirit");
    }

    @Test
    void unblockedFirstStrikerDealsDamageOnlyOnce() {
        addCreatureReady(player1, new VoicelessSpirit());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of());
        resolveCombat();

        harness.assertLife(player2, 18);
        harness.assertOnBattlefield(player1, "Voiceless Spirit");
    }
}
