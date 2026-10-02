package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MangaraOfCorondor.class, AshcoatBear.class, Plains.class})
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
        Permanent mangara = addCreatureReady(player1, new MangaraOfCorondor());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());

        harness.activateAbility(player1, 0, null, bear.getId());
        assertThat(mangara.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mangara);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(mangara.getCard().getId(), bear.getCard().getId());
    }

    @Test
    @DisplayName("Can target Mangara itself")
    void canTargetItself() {
        Permanent mangara = addCreatureReady(player1, new MangaraOfCorondor());

        harness.activateAbility(player1, 0, null, mangara.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mangara);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(mangara.getCard().getId());
    }

    @Test
    @DisplayName("Stays on the battlefield when the target is illegal on resolution")
    void staysWhenTargetLeavesBeforeResolution() {
        Permanent mangara = addCreatureReady(player1, new MangaraOfCorondor());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());

        harness.activateAbility(player1, 0, null, bear.getId());
        gd.playerBattlefields.get(player2.getId()).remove(bear);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(mangara);
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("Can target a noncreature permanent")
    void exilesTargetLand() {
        Permanent mangara = addCreatureReady(player1, new MangaraOfCorondor());
        Permanent plains = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.activateAbility(player1, 0, null, plains.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(mangara);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(plains);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(mangara.getCard().getId(), plains.getCard().getId());
    }

    @Test
    @DisplayName("Still exiles the target when Mangara leaves before resolution")
    void exilesTargetIfSourceLeavesBeforeResolution() {
        Permanent mangara = addCreatureReady(player1, new MangaraOfCorondor());
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());

        harness.activateAbility(player1, 0, null, bear.getId());
        gd.playerBattlefields.get(player1.getId()).remove(mangara);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bear);
        assertThat(gd.exiledCards).extracting(exiled -> exiled.card().getId())
                .contains(bear.getCard().getId());
    }

    @Test
    @DisplayName("Only targets permanents")
    void rejectsPlayerTarget() {
        addCreatureReady(player1, new MangaraOfCorondor());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid target permanent");
    }
}
