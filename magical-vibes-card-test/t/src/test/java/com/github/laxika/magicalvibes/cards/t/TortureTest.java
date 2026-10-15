package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.o.OldGhastbark;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Torture.class, OldGhastbark.class, Forest.class})
class TortureTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Torture attaches it to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creaturePerm = addCreatureReady(player1, new OldGhastbark());

        harness.setHand(player1, List.of(new Torture()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castEnchantment(player1, 0, creaturePerm.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof Torture
                        && p.isAttached()
                        && p.getAttachedTo().equals(creaturePerm.getId()));
    }

    @Test
    @DisplayName("Activating ability puts a -1/-1 counter on the enchanted creature")
    void activatingPutsMinusCounter() {
        Permanent creaturePerm = addCreatureReady(player1, new OldGhastbark());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Torture());
        auraPerm.setAttachedTo(creaturePerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Aura at index 1 (creature at 0, aura at 1)
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creaturePerm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        Permanent creaturePerm = addCreatureReady(player1, new OldGhastbark());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Torture());
        auraPerm.setAttachedTo(creaturePerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(Torture.class);
    }

    @Test
    @DisplayName("Ability can be activated multiple times, stacking -1/-1 counters")
    void abilityStacksCounters() {
        Permanent creaturePerm = addCreatureReady(player1, new OldGhastbark());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Torture());
        auraPerm.setAttachedTo(creaturePerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creaturePerm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Can enchant an opponent's creature and shrink it")
    void canEnchantOpponentCreature() {
        Permanent opponentCreature = addCreatureReady(player2, new OldGhastbark());

        harness.setHand(player1, List.of(new Torture()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castEnchantment(player1, 0, opponentCreature.getId());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        Permanent auraPerm = findPermanent(player1, "Torture");
        int auraIndex = gd.playerBattlefields.get(player1.getId()).indexOf(auraPerm);
        harness.activateAbility(player1, auraIndex, null, null);
        harness.passBothPriorities();

        assertThat(opponentCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot enchant a noncreature permanent")
    void cannotEnchantNoncreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.setHand(player1, List.of(new Torture()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ability does nothing when Torture is no longer attached")
    void abilityDoesNothingWhenAuraBecomesUnattached() {
        Permanent creaturePerm = addCreatureReady(player1, new OldGhastbark());
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Torture());
        auraPerm.setAttachedTo(creaturePerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        auraPerm.setAttachedTo(null);
        harness.passBothPriorities();

        assertThat(creaturePerm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Ability uses the last enchanted creature if Torture leaves before resolution")
    void abilityUsesLastEnchantedCreatureWhenAuraLeaves() {
        Permanent creaturePerm = addCreatureReady(player1, new OldGhastbark());
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Torture());
        auraPerm.setAttachedTo(creaturePerm.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);

        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);
        harness.passBothPriorities();

        assertThat(creaturePerm.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability affects Torture's current enchanted creature when reattached before resolution")
    void abilityUsesCurrentEnchantedCreatureWhenAuraIsReattached() {
        Permanent originalCreature = addCreatureReady(player1, new OldGhastbark());
        Permanent newCreature = addCreatureReady(player1, new OldGhastbark());
        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new Torture());
        auraPerm.setAttachedTo(originalCreature.getId());

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 2, null, null);
        auraPerm.setAttachedTo(newCreature.getId());
        harness.passBothPriorities();

        assertThat(originalCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(newCreature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability requires both its generic and black mana costs")
    void cannotActivateWithOnlyOneBlackMana() {
        Permanent creature = addCreatureReady(player1, new OldGhastbark());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Torture());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Six activations kill the enchanted creature and put Torture in the graveyard")
    void countersReduceToughnessToZero() {
        Permanent creature = addCreatureReady(player2, new OldGhastbark());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Torture());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        for (int i = 0; i < 5; i++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card instanceof OldGhastbark);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Torture);
    }

    @Test
    @DisplayName("A -1/-1 counter cancels an existing +1/+1 counter")
    void minusCounterCancelsPlusCounter() {
        Permanent creature = addCreatureReady(player1, new OldGhastbark());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Torture());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
    }

    @Test
    @DisplayName("Ability cannot put a counter on a creature that has left the battlefield")
    void abilityDoesNothingWhenEnchantedCreatureLeaves() {
        Permanent creature = addCreatureReady(player2, new OldGhastbark());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Torture());
        aura.setAttachedTo(creature.getId());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);

        harness.getPermanentRemovalService().removePermanentToHand(gd, creature);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(creature.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player2.getId())).contains(creature.getCard());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(aura);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof Torture);
    }
}
