package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.b.BeastWithin;
import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SlashPanther;
import com.github.laxika.magicalvibes.cards.v.VaultSkirge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Enslave.class, Demystify.class, FountainOfYouth.class, GrizzlyBears.class,
        BeastWithin.class, SlashPanther.class, VaultSkirge.class})
class EnslaveTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Enslave steals opponent's creature")
    void resolvingStealsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Enslave()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
    }

    @Test
    @DisplayName("At controller's upkeep, enchanted creature deals 1 damage to its owner")
    void upkeepDealsDamageToOwner() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Enslave()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int ownerLifeBefore = gd.playerLifeTotals.get(player2.getId());
        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(ownerLifeBefore - 1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("Upkeep trigger does not fire during opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Enslave()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int ownerLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(ownerLifeBefore);
    }

    @Test
    @DisplayName("Damage accumulates over multiple upkeeps")
    void damageAccumulatesOverUpkeeps() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Enslave()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        int ownerLifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(ownerLifeBefore - 2);
    }

    @Test
    @DisplayName("Creature returns to owner when Enslave is destroyed")
    void creatureReturnsWhenEnslaveDestroyed() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Enslave()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent enslavePerm = findPermanent(player1, "Enslave");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, enslavePerm.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Enslave")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Enslave()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        Permanent artifact = findPermanent(player1, "Fountain of Youth");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enslave can enchant its controller's own creature and damages that player")
    void enchantingOwnCreatureDamagesItsOwner() {
        Permanent creature = addCreatureReady(player1, new SlashPanther());
        harness.setHand(player1, List.of(new Enslave()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Slash Panther");
    }

    @Test
    @DisplayName("The enchanted creature's lifelink gains life for its controller")
    void upkeepDamageUsesEnchantedCreaturesLifelink() {
        Permanent creature = addCreatureReady(player2, new VaultSkirge());
        harness.setHand(player1, List.of(new Enslave()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Destroying Enslave in response does not stop its upkeep damage")
    void upkeepDamageResolvesAfterAuraIsDestroyed() {
        Permanent creature = addCreatureReady(player2, new SlashPanther());
        harness.setHand(player1, List.of(new Enslave()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Enslave");

        harness.setHand(player1, List.of(new BeastWithin()));
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player1, 0, aura.getId());
        harness.assertInGraveyard(player1, "Enslave");
        harness.assertOnBattlefield(player2, "Slash Panther");
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Enslave does not enter the battlefield if its target is destroyed in response")
    void destroyedTargetPreventsAuraFromResolving() {
        Permanent creature = addCreatureReady(player2, new SlashPanther());
        harness.setHand(player1, List.of(new Enslave()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castEnchantment(player1, 0, creature.getId());

        harness.setHand(player2, List.of(new BeastWithin()));
        harness.addMana(player2, ManaColor.GREEN, 3);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Enslave");
        harness.assertNotOnBattlefield(player1, "Enslave");
        harness.assertInGraveyard(player2, "Slash Panther");
    }
}
