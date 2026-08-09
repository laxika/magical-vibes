package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MarkOfSakikoTest extends BaseCardTest {

    @Test
    @DisplayName("Adds green mana equal to combat damage dealt by the enchanted creature")
    void addsManaEqualToCombatDamage() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachMarkOfSakiko(player1, creature);
        harness.setHand(player1, List.of(new GiantGrowth()));
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The generated green mana survives step transitions until end of turn")
    void generatedManaSurvivesStepTransitions() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachMarkOfSakiko(player1, creature);
        harness.setHand(player1, List.of(new GiantGrowth()));
        creature.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.RED, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);

        assertThat(pool.get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(pool.get(ManaColor.RED)).isZero();
    }

    @Test
    @DisplayName("The enchanted creature's controller receives the mana")
    void manaGoesToEnchantedCreatureController() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachMarkOfSakiko(player1, creature);
        harness.setHand(player2, List.of(new GiantGrowth()));
        creature.setAttacking(true);

        resolveCombat(player2);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    @Test
    @DisplayName("A blocked enchanted creature does not generate mana")
    void blockedCreatureDoesNotGenerateMana() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachMarkOfSakiko(player1, creature);
        creature.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
    }

    private void attachMarkOfSakiko(Player controller, Permanent creature) {
        Permanent aura = new Permanent(new MarkOfSakiko());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }
}
