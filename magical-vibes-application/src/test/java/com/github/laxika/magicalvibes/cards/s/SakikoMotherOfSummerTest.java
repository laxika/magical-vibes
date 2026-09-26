package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GnarledMass;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SakikoMotherOfSummer.class, GnarledMass.class})
class SakikoMotherOfSummerTest extends BaseCardTest {

    @Test
    @DisplayName("Adds green mana equal to combat damage dealt by a creature you control")
    void addsManaEqualToAllyCombatDamage() {
        addCreatureReady(player1, new SakikoMotherOfSummer());
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("The generated green mana survives a step transition")
    void generatedManaSurvivesStepTransition() {
        addCreatureReady(player1, new SakikoMotherOfSummer());
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);
        assertThat(pool.get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The generated green mana expires at the end of the turn")
    void generatedManaExpiresAtEndOfTurn() {
        addCreatureReady(player1, new SakikoMotherOfSummer());
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        creature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);

        harness.forceStep(TurnStep.END_STEP);
        gs.advanceStep(gd);
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(3);

        gs.advanceStep(gd);
        assertThat(pool.get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Does not generate mana when the attacking creature is blocked")
    void blockedCreatureDoesNotGenerateMana() {
        addCreatureReady(player1, new SakikoMotherOfSummer());
        Permanent creature = addCreatureReady(player1, new GnarledMass());
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GnarledMass());
        blocker.setBlocking(true);
        blocker.addBlockingTargetId(creature.getId());

        resolveCombat();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("Does not generate mana for combat damage dealt by an opponent's creature")
    void opponentCreatureDoesNotGenerateMana() {
        addCreatureReady(player1, new SakikoMotherOfSummer());
        Permanent creature = addCreatureReady(player2, new GnarledMass());
        creature.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Adds mana for each controlled creature that deals combat damage")
    void addsManaForEachAllyCombatDamage() {
        addCreatureReady(player1, new SakikoMotherOfSummer());
        Permanent firstCreature = addCreatureReady(player1, new GnarledMass());
        Permanent secondCreature = addCreatureReady(player1, new GnarledMass());
        firstCreature.setAttacking(true);
        secondCreature.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(6);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }
}
