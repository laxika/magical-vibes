package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyshipBuccaneer.class, Forest.class})
class SkyshipBuccaneerTest extends BaseCardTest {

    @Test
    @DisplayName("Raid draws a card when Skyship Buccaneer enters")
    void raidDrawsCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castSkyshipBuccaneer();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Raid does not draw a card when no attack occurred")
    void noRaidDoesNotDrawCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        castSkyshipBuccaneer();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertNotInHand(player1, "Forest");
    }

    @Test
    @DisplayName("An opponent's attack does not satisfy raid")
    void opponentsAttackDoesNotDrawCard() {
        harness.setLibrary(player1, List.of(new Forest()));
        gd.playersDeclaredAttackersThisTurn.add(player2.getId());
        castSkyshipBuccaneer();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Raid still draws after Skyship Buccaneer leaves the battlefield")
    void raidDrawsAfterSourceLeavesBattlefield() {
        harness.setLibrary(player1, List.of(new Forest()));
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
        castSkyshipBuccaneer();
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castSkyshipBuccaneer() {
        harness.setHand(player1, List.of(new SkyshipBuccaneer()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

}
