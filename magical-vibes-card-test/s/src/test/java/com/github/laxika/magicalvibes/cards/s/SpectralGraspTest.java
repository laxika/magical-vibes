package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.InvasionOfZendikar;
import com.github.laxika.magicalvibes.cards.n.NahiriTheLithomancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpectralGrasp.class, SkysnareSpider.class, NahiriTheLithomancer.class})
class SpectralGraspTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack the Aura controller")
    void enchantedCreatureCannotAttackAuraController() {
        Permanent creature = addCreatureReady(player1, new SkysnareSpider());
        addAura(player2, creature);

        beginAttack(player1);

        int creatureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(creatureIndex), Map.of(creatureIndex, player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot attack the Aura controller's planeswalker")
    void enchantedCreatureCannotAttackAuraControllersPlaneswalker() {
        Permanent creature = addCreatureReady(player1, new SkysnareSpider());
        addAura(player2, creature);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NahiriTheLithomancer());

        beginAttack(player1);

        int creatureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        assertThatThrownBy(() -> gs.declareAttackers(gd, player1,
                List.of(creatureIndex), Map.of(creatureIndex, planeswalker.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot block creatures controlled by the Aura controller")
    void enchantedCreatureCannotBlockControllersCreature() {
        Permanent enchanted = addCreatureReady(player2, new SkysnareSpider());
        addAura(player1, enchanted);
        Permanent attacker = addCreatureReady(player1, new SkysnareSpider());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(enchanted);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Enchanted creature can't block creatures you control");
    }

    @Test
    @DisplayName("Enchanted creature can block a creature not controlled by the Aura controller")
    void enchantedCreatureCanBlockOpponentsCreature() {
        Permanent enchanted = addCreatureReady(player1, new SkysnareSpider());
        addAura(player1, enchanted);
        Permanent attacker = addCreatureReady(player2, new SkysnareSpider());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player2);

        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(enchanted);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));
    }

    @Test
    @DisplayName("Spectral Grasp's restrictions end when it leaves the battlefield")
    void restrictionsEndWhenRemoved() {
        Permanent creature = addCreatureReady(player1, new SkysnareSpider());
        Permanent aura = addAura(player2, creature);
        gd.playerBattlefields.get(player2.getId()).remove(aura);

        int creatureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        declareAttackers(player1, List.of(creatureIndex));
    }

    @Test
    @DisplayName("Casting Spectral Grasp attaches it to an opposing creature and restricts combat")
    void castingAttachesAndRestrictsCreature() {
        Permanent creature = addCreatureReady(player2, new SkysnareSpider());
        harness.setHand(player1, List.of(new SpectralGrasp()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Spectral Grasp").getAttachedTo()).isEqualTo(creature.getId());
        assertThat(harness.getAttackLegalityService().canAttackDefender(gd, creature, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Enchanting your own creature still lets it attack an opponent")
    void ownEnchantedCreatureCanAttackOpponent() {
        Permanent creature = addCreatureReady(player1, new SkysnareSpider());
        addAura(player1, creature);
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NahiriTheLithomancer());

        assertThat(harness.getAttackLegalityService().canAttackDefender(gd, creature, player2.getId())).isTrue();
        assertThat(harness.getAttackLegalityService().canAttackDefender(gd, creature, planeswalker.getId())).isTrue();
    }

    @Test
    @DisplayName("Attack restrictions follow the Aura's current controller")
    void attackRestrictionFollowsAuraController() {
        Permanent creature = addCreatureReady(player1, new SkysnareSpider());
        Permanent aura = addAura(player2, creature);
        assertThat(harness.getAttackLegalityService().canAttackDefender(gd, creature, player2.getId())).isFalse();

        gd.playerBattlefields.get(player2.getId()).remove(aura);
        gd.playerBattlefields.get(player1.getId()).add(aura);

        assertThat(harness.getAttackLegalityService().canAttackDefender(gd, creature, player2.getId())).isTrue();
    }

    @Test
    @CardUsed(InvasionOfZendikar.class)
    @DisplayName("Enchanted creature may attack a battle controlled by the Aura controller")
    void enchantedCreatureCanAttackAuraControllersBattle() {
        Permanent creature = addCreatureReady(player1, new SkysnareSpider());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfZendikar());
        battle.setProtectorPlayerId(player2.getId());
        assertThat(harness.getAttackLegalityService().canAttackDefender(gd, creature, battle.getId())).isTrue();
        addAura(player1, creature);

        assertThat(harness.getAttackLegalityService().canAttackDefender(gd, creature, battle.getId())).isTrue();
    }

    @Test
    @DisplayName("Spectral Grasp does not prevent an unenchanted creature from blocking")
    void otherCreatureCanBlock() {
        Permanent enchanted = addCreatureReady(player2, new SkysnareSpider());
        addAura(player1, enchanted);
        Permanent blocker = addCreatureReady(player2, new SkysnareSpider());
        Permanent attacker = addCreatureReady(player1, new SkysnareSpider());
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    @Test
    @DisplayName("Blocking restrictions follow the Aura's current controller")
    void blockingRestrictionFollowsAuraController() {
        Permanent enchanted = addCreatureReady(player2, new SkysnareSpider());
        Permanent aura = addAura(player1, enchanted);
        Permanent attacker = addCreatureReady(player1, new SkysnareSpider());
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerBattlefields.get(player2.getId()).add(aura);
        attacker.setAttacking(true);
        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(enchanted),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }

    private Permanent addAura(Player controller, Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new SpectralGrasp());
        aura.setAttachedTo(enchanted.getId());
        return aura;
    }

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

}
