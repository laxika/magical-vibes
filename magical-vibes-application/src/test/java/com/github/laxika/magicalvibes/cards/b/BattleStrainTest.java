package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DwarvenGrunt;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PalaceGuard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({BattleStrain.class, DwarvenGrunt.class, Ornithopter.class})
class BattleStrainTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 1 damage to a blocking creature's controller")
    void dealsDamageToBlockingCreaturesController() {
        Permanent attacker = addCreatureReady(player1, new DwarvenGrunt());
        attacker.setAttacking(true);
        addCreatureReady(player2, new DwarvenGrunt());
        harness.addToBattlefield(player1, new BattleStrain());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Triggers once for each blocking creature")
    void triggersOncePerBlocker() {
        Permanent attacker1 = addCreatureReady(player1, new DwarvenGrunt());
        attacker1.setAttacking(true);
        Permanent attacker2 = addCreatureReady(player1, new DwarvenGrunt());
        attacker2.setAttacking(true);
        addCreatureReady(player2, new DwarvenGrunt());
        addCreatureReady(player2, new DwarvenGrunt());
        harness.addToBattlefield(player1, new BattleStrain());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 1)));
        resolveAllTriggers();

        harness.assertLife(player2, 18);
    }

    @Test
    @CardUsed(PalaceGuard.class)
    @DisplayName("Triggers only once when one creature blocks multiple attackers")
    void triggersOnceWhenOneCreatureBlocksMultipleAttackers() {
        Permanent attacker1 = addCreatureReady(player1, new DwarvenGrunt());
        attacker1.setAttacking(true);
        Permanent attacker2 = addCreatureReady(player1, new DwarvenGrunt());
        attacker2.setAttacking(true);
        addCreatureReady(player2, new PalaceGuard());
        harness.addToBattlefield(player1, new BattleStrain());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(0, 1)));
        resolveAllTriggers();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Declaring no blockers does not trigger Battle Strain")
    void noBlockersNoTrigger() {
        Permanent attacker = addCreatureReady(player1, new Ornithopter());
        attacker.setAttacking(true);
        harness.addToBattlefield(player1, new BattleStrain());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }
}
