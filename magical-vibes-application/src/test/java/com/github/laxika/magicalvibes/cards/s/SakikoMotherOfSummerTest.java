package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class SakikoMotherOfSummerTest extends BaseCardTest {

    @Test
    @DisplayName("Adds green mana equal to combat damage dealt by a creature you control")
    void addsManaEqualToAllyCombatDamage() {
        addCreatureReady(player1, new SakikoMotherOfSummer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        resolveCombat();
        harness.getStackResolutionService().resolveTopOfStack(gd);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The generated green mana survives a step transition")
    void generatedManaSurvivesStepTransition() {
        addCreatureReady(player1, new SakikoMotherOfSummer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        resolveCombat();
        harness.getStackResolutionService().resolveTopOfStack(gd);

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(pool.get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("Does not generate mana when the attacking creature is blocked")
    void blockedCreatureDoesNotGenerateMana() {
        addCreatureReady(player1, new SakikoMotherOfSummer());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(bears.getId());

        resolveCombat();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }
}
