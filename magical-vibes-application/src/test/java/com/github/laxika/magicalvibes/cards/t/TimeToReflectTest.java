package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.d.DuneBeetle;
import com.github.laxika.magicalvibes.cards.m.MiasmicMummy;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.a.AmoeboidChangeling;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TimeToReflect.class, DuneBeetle.class, MiasmicMummy.class, GiantSpider.class, AmoeboidChangeling.class})
class TimeToReflectTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles a creature that blocked a Zombie this turn")
    void exilesCreatureThatBlockedAZombie() {
        Permanent zombie = addCreatureReady(player1, new MiasmicMummy());
        zombie.setAttacking(true);
        addCreatureReady(player2, new DuneBeetle()); // non-Zombie blocker

        declareBlock();

        UUID blockerId = harness.getPermanentId(player2, "Dune Beetle");
        castAndResolveTimeToReflect(player1, blockerId);

        harness.assertNotOnBattlefield(player2, "Dune Beetle");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Dune Beetle"));
    }

    @Test
    @DisplayName("Exiles a creature that was blocked by a Zombie this turn")
    void exilesCreatureThatWasBlockedByAZombie() {
        Permanent attacker = addCreatureReady(player1, new DuneBeetle()); // non-Zombie attacker
        attacker.setAttacking(true);
        addCreatureReady(player2, new MiasmicMummy()); // Zombie blocker

        declareBlock();

        UUID attackerId = harness.getPermanentId(player1, "Dune Beetle");
        castAndResolveTimeToReflect(player1, attackerId);

        harness.assertNotOnBattlefield(player1, "Dune Beetle");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dune Beetle"));
    }

    @Test
    @DisplayName("Cannot target a creature that only blocked a non-Zombie")
    void cannotTargetCreatureThatBlockedNonZombie() {
        Permanent attacker = addCreatureReady(player1, new DuneBeetle()); // non-Zombie attacker
        attacker.setAttacking(true);
        addCreatureReady(player2, new GiantSpider()); // non-Zombie blocker

        declareBlock();

        UUID blockerId = harness.getPermanentId(player2, "Giant Spider");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new TimeToReflect()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, blockerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCreatureThatDidNotBlock() {
        Permanent target = addCreatureReady(player2, new DuneBeetle());
        harness.setHand(player1, List.of(new TimeToReflect()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void cannotTargetCreatureBlockedByFormerZombie() {
        Permanent attacker = addCreatureReady(player1, new DuneBeetle());
        addCreatureReady(player1, new AmoeboidChangeling());
        Permanent blocker = addCreatureReady(player2, new MiasmicMummy());
        harness.activateAbility(player1, 1, 1, null, blocker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        declareBlock();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(player1, List.of(new TimeToReflect()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, attacker.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void exilesCreatureBlockedByCreatureGrantedZombieType() {
        Permanent attacker = addCreatureReady(player1, new DuneBeetle());
        addCreatureReady(player1, new AmoeboidChangeling());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        harness.activateAbility(player1, 1, 0, null, blocker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(true);
        declareBlock();

        castAndResolveTimeToReflect(player1, attacker.getId());
        harness.assertNotOnBattlefield(player1, "Dune Beetle");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dune Beetle"));
    }

    @Test
    void canExileBeforeCombatDamage() {
        Permanent attacker = addCreatureReady(player1, new DuneBeetle());
        attacker.setAttacking(true);
        addCreatureReady(player2, new MiasmicMummy());
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS, this::declareBlock);
        harness.setHand(player1, List.of(new TimeToReflect()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Dune Beetle");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dune Beetle"));
    }

    @Test
    void remainsEligibleWhenZombieLosesItsTypesAfterBlocking() {
        Permanent attacker = addCreatureReady(player1, new DuneBeetle());
        addCreatureReady(player1, new AmoeboidChangeling());
        Permanent blocker = addCreatureReady(player2, new MiasmicMummy());
        attacker.setAttacking(true);
        declareBlock();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.activateAbility(player1, 1, 1, null, blocker.getId());
        harness.passBothPriorities();

        castAndResolveTimeToReflect(player1, attacker.getId());

        harness.assertNotOnBattlefield(player1, "Dune Beetle");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Dune Beetle"));
    }
    private void declareBlock() {
        prepareDeclareBlockers(player1);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    private void castAndResolveTimeToReflect(Player caster, UUID targetId) {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.setHand(caster, List.of(new TimeToReflect()));
        harness.addMana(caster, ManaColor.WHITE, 1);
        harness.castAndResolveInstant(caster, 0, targetId);
    }
}
