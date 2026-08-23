package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MangaraOfCorondor.class, GrizzlyBears.class})
class MangaraOfCorondorTest extends BaseCardTest {

    @BeforeEach
    void mainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Exiles Mangara and the targeted permanent")
    void exilesItselfAndTargetPermanent() {
        Permanent mangara = addReadyMangara();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        assertThat(mangara.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mangara);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(mangara.getCard().getId(), bears.getCard().getId());
    }

    @Test
    @DisplayName("Can target Mangara itself")
    void canTargetItself() {
        Permanent mangara = addReadyMangara();

        harness.activateAbility(player1, 0, null, mangara.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mangara);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(mangara.getCard().getId());
    }

    @Test
    @DisplayName("Stays on the battlefield when the target is illegal on resolution")
    void staysWhenTargetLeavesBeforeResolution() {
        Permanent mangara = addReadyMangara();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, bears.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mangara);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Only targets permanents")
    void rejectsPlayerTarget() {
        addReadyMangara();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target permanent");
    }

    private Permanent addReadyMangara() {
        Permanent mangara = harness.addToBattlefieldAndReturn(player1, new MangaraOfCorondor());
        mangara.setSummoningSick(false);
        return mangara;
    }
}
