package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CanopyCoverTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature cannot be blocked by a creature without flying or reach")
    void cannotBeBlockedByCreatureWithoutFlyingOrReach() {
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        enchant(attacker);
        Permanent blocker = addReadyCreature(player2);

        beginDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying or reach");
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by creatures with flying or reach")
    void canBeBlockedByFlyingOrReach() {
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        enchant(attacker);
        Permanent flyingBlocker = new Permanent(new AvenFisher());
        flyingBlocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(flyingBlocker);
        Permanent reachBlocker = new Permanent(new GiantSpider());
        reachBlocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(reachBlocker);

        beginDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(flyingBlocker),
                        gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(flyingBlocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can be blocked by a creature with reach")
    void canBeBlockedByReach() {
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        enchant(attacker);
        Permanent blocker = new Permanent(new GiantSpider());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        beginDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature cannot be targeted by opponents' spells or abilities")
    void cannotBeTargetedByOpponentsSpellsOrAbilities() {
        Permanent target = addReadyCreature(player1);
        enchant(target);

        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castInstant(player2, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent pyromancer = new Permanent(new ProdigalPyromancer());
        pyromancer.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(pyromancer);
        assertThatThrownBy(() -> harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(pyromancer), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Enchanted creature can be targeted by its controller")
    void canBeTargetedByItsController() {
        Permanent target = addReadyCreature(player1);
        enchant(target);

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.getPowerModifier()).isEqualTo(3);
        assertThat(target.getToughnessModifier()).isEqualTo(3);
    }

    private Permanent addReadyCreature(Player player) {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(creature);
        return creature;
    }

    private void enchant(Permanent creature) {
        Permanent aura = new Permanent(new CanopyCover());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    private void beginDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
