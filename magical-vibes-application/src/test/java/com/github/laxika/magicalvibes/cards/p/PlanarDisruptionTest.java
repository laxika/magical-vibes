package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PlanarDisruptionTest extends BaseCardTest {

    @Test
    @DisplayName("Planar Disruption can enchant an artifact and lock its activated abilities")
    void canEnchantArtifactAndLockAbilities() {
        Permanent artifact = new Permanent(new FountainOfYouth());
        gd.playerBattlefields.get(player2.getId()).add(artifact);

        harness.setHand(player1, List.of(new PlanarDisruption()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && permanent.getAttachedTo().equals(artifact.getId()));

        harness.addMana(player2, ManaColor.COLORLESS, 2);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Planar Disruption prevents an enchanted creature from attacking")
    void enchantedCreatureCannotAttack() {
        Permanent creature = new Permanent(new GrizzlyBears());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        Permanent aura = new Permanent(new PlanarDisruption());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Planar Disruption prevents an enchanted creature from blocking")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent aura = new Permanent(new PlanarDisruption());
        aura.setAttachedTo(blocker.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Planar Disruption prevents an enchanted creature from activating abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent creature = new Permanent(new BottleGnomes());
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        Permanent aura = new Permanent(new PlanarDisruption());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player2.getId()).add(aura);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Planar Disruption can enchant a planeswalker")
    void canEnchantPlaneswalker() {
        Permanent planeswalker = new Permanent(new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        gd.playerBattlefields.get(player2.getId()).add(planeswalker);

        harness.setHand(player1, List.of(new PlanarDisruption()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && permanent.getAttachedTo().equals(planeswalker.getId()));
    }

    @Test
    @DisplayName("Planar Disruption cannot enchant a land")
    void cannotEnchantLand() {
        Permanent land = new Permanent(new Forest());
        gd.playerBattlefields.get(player2.getId()).add(land);

        harness.setHand(player1, List.of(new PlanarDisruption()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or planeswalker");
    }
}
