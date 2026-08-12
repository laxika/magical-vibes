package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.ArcticFoxes;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Melting has no effect of its own to observe, so its behaviour is asserted through a card that
 * cares whether a land is snow: Arctic Foxes can't be blocked by power 2+ creatures while the
 * defending player controls a snow land.
 */
class MeltingTest extends BaseCardTest {

    private Permanent blocker;
    private Permanent fox;

    private Permanent snowPlainsOnDefender() {
        Permanent snowLand = new Permanent(new Plains());
        TestCards.mutableCard(snowLand).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player2.getId()).add(snowLand);
        return snowLand;
    }

    private void setUpCombat() {
        blocker = new Permanent(new HillGiant());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        fox = new Permanent(new ArcticFoxes());
        fox.setSummoningSick(false);
        fox.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(fox);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
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
}
