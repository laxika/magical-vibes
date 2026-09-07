package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ControlMagic.class, Disenchant.class, Forest.class, GrizzlyBears.class})
class ControlMagicTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Control Magic steals opponent's creature")
    void resolvingStealsCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        ControlMagic controlMagic = new ControlMagic();

        harness.setHand(player1, List.of(controlMagic));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        // Creature should now be on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));

        // Control Magic aura should be attached to the creature on player1's battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() == controlMagic
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));

        // Creature should be tracked as stolen
        assertThat(gd.stolenCreatures).containsEntry(creature.getId(), player2.getId());
    }

    @Test
    @DisplayName("Can enchant a creature its controller already controls")
    void canEnchantOwnCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new ControlMagic()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof ControlMagic
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
        assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
    }

    @Test
    @DisplayName("Control Magic fizzles if target creature is no longer on the battlefield")
    void fizzlesIfTargetGone() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        ControlMagic controlMagic = new ControlMagic();

        harness.setHand(player1, List.of(controlMagic));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());

        // Remove the creature before resolution
        gd.playerBattlefields.get(player2.getId()).remove(creature);

        harness.passBothPriorities();

        // Control Magic should be in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(controlMagic);
    }

    @Test
    @DisplayName("Creature returns to owner when Control Magic is destroyed")
    void creatureReturnsWhenControlMagicDestroyed() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        ControlMagic controlMagic = new ControlMagic();

        harness.setHand(player1, List.of(controlMagic));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));

        Permanent auraPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard() == controlMagic)
                .findFirst()
                .orElseThrow();

        // Set up for Disenchant: force step to a main phase, give player2 priority
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, auraPerm.getId());
        harness.passBothPriorities();

        // Creature should return to player2's battlefield
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));

        assertThat(gd.stolenCreatures).doesNotContainKey(creature.getId());
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Control Magic")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new ControlMagic()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
