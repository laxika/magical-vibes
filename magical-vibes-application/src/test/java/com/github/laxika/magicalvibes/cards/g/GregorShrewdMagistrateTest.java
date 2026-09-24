package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AnthemOfChampions;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GregorShrewdMagistrate.class, AnthemOfChampions.class, Forest.class, HillGiant.class, Memnite.class})
class GregorShrewdMagistrateTest extends BaseCardTest {

    @Test
    @DisplayName("Draws cards equal to its power when it deals combat damage")
    void drawsCardsEqualToPower() {
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        harness.addToBattlefield(player1, new AnthemOfChampions());
        Permanent gregor = addCreatureReady(player1, new GregorShrewdMagistrate());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(gregor)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Skulk prevents a greater-power creature from blocking")
    void skulkPreventsGreaterPowerCreatureFromBlocking() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent gregor = addCreatureReady(player1, new GregorShrewdMagistrate());
        gregor.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int gregorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(gregor);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, gregorIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("skulk");
    }

    @Test
    @DisplayName("Skulk allows an equal-power creature to block")
    void skulkAllowsEqualPowerCreatureToBlock() {
        Permanent blocker = addCreatureReady(player2, new Memnite());
        Permanent gregor = addCreatureReady(player1, new GregorShrewdMagistrate());
        gregor.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int gregorIndex = gd.playerBattlefields.get(player1.getId()).indexOf(gregor);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, gregorIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
