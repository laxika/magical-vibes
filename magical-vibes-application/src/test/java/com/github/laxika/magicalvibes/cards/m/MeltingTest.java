package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArcticFoxes;
import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.cards.w.WoollyMammoths;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Melting has no effect of its own to observe, so its behaviour is asserted through a card that
 * cares whether a land is snow: Arctic Foxes can't be blocked by power 2+ creatures while the
 * defending player controls a snow land.
 */
@CardUsed({ArcticFoxes.class, BalduvianBarbarians.class, Melting.class, SnowCoveredPlains.class,
        WoollyMammoths.class})
class MeltingTest extends BaseCardTest {

    private Permanent blocker;
    private Permanent fox;

    private void snowPlainsOnDefender() {
        harness.addToBattlefield(player2, new SnowCoveredPlains());
    }

    private void setUpCombat() {
        blocker = addCreatureReady(player2, new BalduvianBarbarians());

        fox = addCreatureReady(player1, new ArcticFoxes());
        fox.setAttacking(true);

        prepareDeclareBlockers();
    }

    private void declareBlock() {
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(fox);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));
    }

    @Test
    @DisplayName("Without Melting, the snow land keeps the block restriction on")
    void snowLandRestrictsBlockWithoutMelting() {
        snowPlainsOnDefender();
        setUpCombat();

        assertThatThrownBy(this::declareBlock).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Melting makes the snow land no longer snow")
    void meltingRemovesSnowFromLands() {
        snowPlainsOnDefender();
        harness.addToBattlefield(player1, new Melting());
        setUpCombat();

        declareBlock();

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Melting also removes snow from lands entering after it")
    void meltingAppliesToLandsEnteringLater() {
        harness.addToBattlefield(player1, new Melting());
        snowPlainsOnDefender();
        setUpCombat();

        declareBlock();

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Melting is symmetric — it also strips snow from its controller's opponent's lands")
    void meltingAppliesUnderOpponentControl() {
        snowPlainsOnDefender();
        harness.addToBattlefield(player2, new Melting());
        setUpCombat();

        declareBlock();

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Without Melting, a snow land enables snow-dependent static abilities")
    void snowLandEnablesSnowDependentStaticAbility() {
        harness.addToBattlefield(player1, new SnowCoveredPlains());
        Permanent mammoths = addCreatureReady(player1, new WoollyMammoths());

        assertThat(gqs.hasKeyword(gd, mammoths, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Melting also disables snow-dependent static abilities")
    void meltingDisablesSnowDependentStaticAbility() {
        harness.addToBattlefield(player1, new Melting());
        harness.addToBattlefield(player1, new SnowCoveredPlains());
        Permanent mammoths = addCreatureReady(player1, new WoollyMammoths());

        assertThat(gqs.hasKeyword(gd, mammoths, Keyword.TRAMPLE)).isFalse();
    }
}
