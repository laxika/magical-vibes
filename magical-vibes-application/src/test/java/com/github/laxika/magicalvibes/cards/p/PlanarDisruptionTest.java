package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NicolBolasPlaneswalker;
import com.github.laxika.magicalvibes.cards.m.MindStone;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PlanarDisruption.class, BottleGnomes.class, FountainOfYouth.class, Forest.class,
        GrizzlyBears.class, NicolBolasPlaneswalker.class, MindStone.class, Demystify.class})
class PlanarDisruptionTest extends BaseCardTest {

    @Test
    @DisplayName("Planar Disruption can enchant an artifact and lock its activated abilities")
    void canEnchantArtifactAndLockAbilities() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new PlanarDisruption());
        aura.setAttachedTo(creature.getId());

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
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        blocker.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player1, new PlanarDisruption());
        aura.setAttachedTo(blocker.getId());

        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

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
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        creature.setSummoningSick(false);

        Permanent aura = harness.addToBattlefieldAndReturn(player2, new PlanarDisruption());
        aura.setAttachedTo(creature.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Planar Disruption can enchant a planeswalker")
    void canEnchantPlaneswalker() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

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
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.setHand(player1, List.of(new PlanarDisruption()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an artifact, creature, or planeswalker");
    }

    @Test
    void enchantedArtifactCannotActivateManaAbility() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new MindStone());
        harness.setHand(player1, List.of(new PlanarDisruption()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(artifact.isTapped()).isFalse();
    }

    @Test
    void enchantedPlaneswalkerCannotActivateLoyaltyAbility() {
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new NicolBolasPlaneswalker());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setHand(player1, List.of(new PlanarDisruption()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, planeswalker.getId());
        harness.passBothPriorities();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
    }

    @Test
    void destroyingAuraRestoresEnchantedCreatureAbility() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        harness.setHand(player1, List.of(new PlanarDisruption(), new Demystify()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Planar Disruption"));
        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertLife(player1, 23);
        harness.assertInGraveyard(player1, "Bottle Gnomes");
        harness.assertInGraveyard(player1, "Planar Disruption");
    }

    @Test
    void abilityActivatedInResponseStillResolves() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new PlanarDisruption()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, artifact.getId());
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.assertLife(player2, 21);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> artifact.getId().equals(permanent.getAttachedTo()));
    }
}
