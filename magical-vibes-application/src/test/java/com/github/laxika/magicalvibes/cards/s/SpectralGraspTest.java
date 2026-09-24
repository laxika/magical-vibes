package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
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

@CardUsed({SpectralGrasp.class, GrizzlyBears.class})
class SpectralGraspTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot attack the Aura controller")
    void enchantedCreatureCannotAttackAuraController() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
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
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addAura(player2, creature);
        Permanent planeswalker = addPlaneswalker(player2);

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
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        addAura(player1, enchanted);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
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
        Permanent enchanted = addCreatureReady(player1, new GrizzlyBears());
        addAura(player1, enchanted);
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
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
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = addAura(player2, creature);
        gd.playerBattlefields.get(player2.getId()).remove(aura);

        beginAttack(player1);

        int creatureIndex = gd.playerBattlefields.get(player1.getId()).indexOf(creature);
        gs.declareAttackers(gd, player1,
                List.of(creatureIndex), Map.of(creatureIndex, player2.getId()));
    }

    private Permanent addAura(Player controller, Permanent enchanted) {
        Permanent aura = new Permanent(new SpectralGrasp());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
        return aura;
    }

    private void beginAttack(Player attacker) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }

    private Permanent addPlaneswalker(Player player) {
        Card card = new Card();
        card.setName("Test Planeswalker");
        card.setType(CardType.PLANESWALKER);
        Permanent permanent = new Permanent(card);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
