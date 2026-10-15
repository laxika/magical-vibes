package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Snag.class, Forest.class, GrizzlyBears.class})
class SnagTest extends BaseCardTest {

    @Test
    @DisplayName("Can be cast by discarding a Forest")
    void canBeCastByDiscardingForest() {
        harness.setHand(player1, List.of(new Snag(), new Forest()));

        harness.castInstantWithDiscard(player1, 0, null, 1);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Snag");
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Alternate cost requires a Forest card")
    void alternateCostRequiresForest() {
        harness.setHand(player1, List.of(new Snag(), new GrizzlyBears()));

        assertThatThrownBy(() -> harness.castInstantWithDiscard(player1, 0, null, 1))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevents combat damage from unblocked creatures but not blocked creatures")
    void preventsDamageFromUnblockedCreatures() {
        harness.setLife(player2, 20);
        Permanent unblockedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blockedAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player1, List.of(
                gd.playerBattlefields.get(player1.getId()).indexOf(unblockedAttacker),
                gd.playerBattlefields.get(player1.getId()).indexOf(blockedAttacker)));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(blockedAttacker))));

        harness.setHand(player1, List.of(new Snag(), new Forest()));
        harness.castInstantWithDiscard(player1, 0, null, 1);
        harness.passBothPriorities();
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can pay the normal mana cost without discarding a Forest")
    void canPayNormalManaCost() {
        harness.castFromHand(player1, new Snag(), "{3}{G}");
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Snag");
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Defender can cast Snag before combat and only unblocked damage is prevented")
    void defenderCanCastBeforeCombat() {
        harness.setLife(player2, 20);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        harness.castFromHand(player2, new Snag(), "{3}{G}");
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0, 1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(1);
    }
}
