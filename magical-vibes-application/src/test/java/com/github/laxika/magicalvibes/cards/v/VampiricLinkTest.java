package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({VampiricLink.class, ProdigalPyromancer.class})
class VampiricLinkTest extends BaseCardTest {

    @Test
    @DisplayName("You gain life equal to combat damage dealt by the enchanted creature")
    void gainsLifeFromCombatDamage() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        ProdigalPyromancer card = new ProdigalPyromancer();
        card.setPower(3);
        Permanent creature = addCreatureReady(player1, card);
        attachVampiricLink(creature);

        declareAttackers(player1, List.of(gd.playerBattlefields.get(player1.getId()).indexOf(creature)));
        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 17);
    }

    @Test
    @DisplayName("You gain life equal to noncombat damage dealt by the enchanted creature")
    void gainsLifeFromNoncombatDamage() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        Permanent creature = addCreatureReady(player2, new ProdigalPyromancer());
        attachVampiricLink(creature);

        harness.activateAbility(player2, 0, null, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("You gain life when enchanted creature deals combat damage to a creature")
    void gainsLifeFromCombatDamageToCreatureEvenWhenTheEnchantedCreatureDies() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        ProdigalPyromancer attackerCard = new ProdigalPyromancer();
        attackerCard.setPower(3);
        Permanent attacker = addCreatureReady(player2, attackerCard);
        Permanent blocker = addCreatureReady(player1, new ProdigalPyromancer());
        attachVampiricLink(attacker);

        declareAttackers(player2, List.of(gd.playerBattlefields.get(player2.getId()).indexOf(attacker)));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player1.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player2.getId()).indexOf(attacker))));
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 13);
        harness.assertLife(player2, 20);
        harness.assertNotOnBattlefield(player1, "Prodigal Pyromancer");
        harness.assertNotOnBattlefield(player2, "Prodigal Pyromancer");
    }

    private void attachVampiricLink(Permanent creature) {
        harness.setHand(player1, List.of(new VampiricLink()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
    }
}
