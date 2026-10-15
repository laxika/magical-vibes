package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.r.RimeboundDead;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GoblinFurrier.class, RimeboundDead.class, Humility.class})
class GoblinFurrierTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents its combat damage to snow creatures")
    void preventsCombatDamageToSnowCreature() {
        Permanent furrier = addCreatureReady(player1, new GoblinFurrier());
        Permanent snowCreature = addCreatureReady(player2, new RimeboundDead());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(snowCreature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(snowCreature);
        assertThat(furrier.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not prevent its combat damage to nonsnow creatures")
    void doesNotPreventCombatDamageToNonsnowCreature() {
        Permanent furrier = addCreatureReady(player1, new GoblinFurrier());
        Permanent nonsnowCreature = addCreatureReady(player2, new GoblinFurrier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(nonsnowCreature);
        harness.assertInGraveyard(player2, "Goblin Furrier");
        assertThat(furrier.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not prevent damage to snow creatures from other sources")
    void doesNotPreventDamageFromOtherSources() {
        addCreatureReady(player1, new GoblinFurrier());
        Permanent otherSource = addCreatureReady(player1, new RimeboundDead());
        addCreatureReady(player2, new RimeboundDead());

        declareAttackersAndPrepareBlockers(List.of(1));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Goblin Furrier");
        harness.assertInGraveyard(player2, "Rimebound Dead");
        assertThat(otherSource).isNotIn(gd.playerBattlefields.get(player1.getId()));
    }

    @Test
    @DisplayName("Prevents damage to a snow attacker when blocking")
    void preventsDamageToSnowAttacker() {
        Permanent snowAttacker = addCreatureReady(player1, new RimeboundDead());
        Permanent furrier = addCreatureReady(player2, new GoblinFurrier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(snowAttacker.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(snowAttacker);
        assertThat(furrier.getMarkedDamage()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(furrier);
    }

    @Test
    @DisplayName("Does not prevent combat damage to a player")
    void dealsCombatDamageToPlayer() {
        addCreatureReady(player1, new GoblinFurrier());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Does not prevent damage to snow creatures after losing its abilities")
    void doesNotPreventDamageAfterLosingAbilities() {
        Permanent furrier = addCreatureReady(player1, new GoblinFurrier());
        Permanent snowCreature = addCreatureReady(player2, new RimeboundDead());
        harness.addToBattlefield(player1, new Humility());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(furrier);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(snowCreature);
        harness.assertInGraveyard(player1, "Goblin Furrier");
        harness.assertInGraveyard(player2, "Rimebound Dead");
    }
}
