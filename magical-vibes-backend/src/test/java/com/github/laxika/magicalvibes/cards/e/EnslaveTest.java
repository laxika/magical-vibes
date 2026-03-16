package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.d.Demystify;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ControlEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.EnchantedCreatureDealsDamageToItsOwnerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class EnslaveTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Enslave has control and upkeep damage effects")
    void hasCorrectEffects() {
        Enslave card = new Enslave();

        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst())
                .isInstanceOf(ControlEnchantedCreatureEffect.class);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(EnchantedCreatureDealsDamageToItsOwnerEffect.class);
    }

    // ===== Control effect =====

    @Test
    @DisplayName("Resolving Enslave steals opponent's creature")
    void resolvingStealsCreature() {
        Permanent creature = addCreatureReady(player2);

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

    // ===== Upkeep damage trigger =====

    @Test
    @DisplayName("At controller's upkeep, enchanted creature deals 1 damage to its owner")
    void upkeepDealsDamageToOwner() {
        Permanent creature = addCreatureReady(player2);

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
        Permanent creature = addCreatureReady(player2);

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
        Permanent creature = addCreatureReady(player2);

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

    // ===== Creature returns when Enslave is destroyed =====

    @Test
    @DisplayName("Creature returns to owner when Enslave is destroyed")
    void creatureReturnsWhenEnslaveDestroyed() {
        Permanent creature = addCreatureReady(player2);

        harness.setHand(player1, List.of(new Enslave()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent enslavePerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Enslave"))
                .findFirst().orElseThrow();

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Demystify()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, enslavePerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Cannot target a noncreature permanent with Enslave")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new Enslave()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        Permanent artifact = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Fountain of Youth"))
                .findFirst().orElseThrow();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Helper methods =====

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addCreatureReady(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
