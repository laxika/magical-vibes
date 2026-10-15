package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.m.MoriokReaver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VulshokHeartstoker.class, MoriokReaver.class})
class VulshokHeartstokerTest extends BaseCardTest {

    @Test
    @DisplayName("Creature spell has no target")
    void creatureSpellHasNoTarget() {
        harness.addToBattlefield(player2, new MoriokReaver());
        harness.setHand(player1, List.of(new VulshokHeartstoker()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Vulshok Heartstoker");
        assertThat(entry.getTargetId()).isNull();
    }

    @Test
    @DisplayName("Resolving creature spell puts ETB trigger on stack")
    void resolvingPutsEtbOnStack() {
        harness.addToBattlefield(player2, new MoriokReaver());
        harness.setHand(player1, List.of(new VulshokHeartstoker()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Moriok Reaver");
        harness.castCreature(player1, 0);

        // Resolve the creature spell.
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        harness.assertOnBattlefield(player1, "Vulshok Heartstoker");

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Vulshok Heartstoker");
        assertThat(trigger.getTargetId()).isEqualTo(targetId);
    }

    @Test
    @DisplayName("ETB resolves and gives target creature +2/+0")
    void etbBoostsTargetCreature() {
        harness.addToBattlefield(player2, new MoriokReaver());
        harness.setHand(player1, List.of(new VulshokHeartstoker()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Moriok Reaver");
        harness.castCreature(player1, 0);

        // Resolve the creature spell.
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);
        // Resolve the triggered ability.
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();

        Permanent target = gqs.findPermanentById(gd, targetId);
        assertThat(target.getPowerModifier()).isEqualTo(2);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player2, new MoriokReaver());
        harness.setHand(player1, List.of(new VulshokHeartstoker()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Moriok Reaver");
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // Resolve creature
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities(); // Resolve ETB

        Permanent target = gqs.findPermanentById(gd, targetId);
        assertThat(target.getPowerModifier()).isEqualTo(2);

        // Advance through cleanup to the next turn.
        harness.passUntil(TurnStep.UPKEEP);

        assertThat(target.getPowerModifier()).isEqualTo(0);
        assertThat(target.getToughnessModifier()).isEqualTo(0);
        assertThat(target.getEffectivePower()).isEqualTo(3);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Can target own creature")
    void canTargetOwnCreature() {
        harness.addToBattlefield(player1, new MoriokReaver());
        harness.setHand(player1, List.of(new VulshokHeartstoker()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player1, "Moriok Reaver");
        harness.castCreature(player1, 0);

        harness.passBothPriorities(); // Resolve creature
        harness.handlePermanentChosen(player1, targetId);
        harness.passBothPriorities(); // Resolve ETB

        Permanent target = gqs.findPermanentById(gd, targetId);
        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("ETB fizzles if target creature is removed before resolution")
    void etbFizzlesIfTargetRemoved() {
        harness.addToBattlefield(player2, new MoriokReaver());
        harness.setHand(player1, List.of(new VulshokHeartstoker()));
        harness.addMana(player1, ManaColor.RED, 3);

        UUID targetId = harness.getPermanentId(player2, "Moriok Reaver");
        harness.castCreature(player1, 0);

        // Resolve the creature spell.
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, targetId);

        // Remove target before ETB resolves
        gd.playerBattlefields.get(player2.getId()).clear();

        // Resolve the triggered ability.
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Can cast without a target when no creatures on battlefield")
    void canCastWithoutTarget() {
        harness.setHand(player1, List.of(new VulshokHeartstoker()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Vulshok Heartstoker");
    }

    @Test
    @DisplayName("ETB can target itself on an empty battlefield")
    void etbCanTargetItselfOnEmptyBattlefield() {
        harness.setHand(player1, List.of(new VulshokHeartstoker()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0);

        // Resolve the creature spell.
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vulshok Heartstoker");
        UUID selfId = harness.getPermanentId(player1, "Vulshok Heartstoker");
        harness.handlePermanentChosen(player1, selfId);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gqs.findPermanentById(gd, selfId).getEffectivePower()).isEqualTo(4);
        assertThat(gqs.findPermanentById(gd, selfId).getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("A stale cast-time selection must not replace choosing the ETB target")
    void choosesEtbTargetAfterEnteringDespiteStaleCastTimeSelection() {
        Permanent oldTarget = harness.addToBattlefieldAndReturn(player2, new MoriokReaver());
        harness.setHand(player1, List.of(new VulshokHeartstoker()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castCreature(player1, 0, oldTarget.getId());
        gd.playerBattlefields.get(player2.getId()).remove(oldTarget);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vulshok Heartstoker");
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        UUID selfId = harness.getPermanentId(player1, "Vulshok Heartstoker");
        harness.handlePermanentChosen(player1, selfId);
        harness.passBothPriorities();

        assertThat(gqs.findPermanentById(gd, selfId).getEffectivePower()).isEqualTo(4);
    }

    @Test
    @DisplayName("Entering without being cast still triggers the boost")
    void enteringWithoutBeingCastTriggersBoost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoriokReaver());
        harness.enterBattlefieldAndReturn(player1, new VulshokHeartstoker());

        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing Heartstoker does not stop its triggered ability")
    void triggerResolvesAfterSourceLeaves() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new MoriokReaver());
        harness.setHand(player1, List.of(new VulshokHeartstoker()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(target.getEffectivePower()).isEqualTo(5);
        assertThat(target.getEffectiveToughness()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
