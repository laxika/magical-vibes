package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CageOfHands;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
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

@CardUsed({MysticRestraints.class, WanderingOnes.class, CageOfHands.class})
class MysticRestraintsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Mystic Restraints taps the enchanted creature and attaches")
    void resolvingTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new WanderingOnes());

        harness.setHand(player1, List.of(new MysticRestraints()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB tap trigger

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mystic Restraints")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent enchanted = addCreatureReady(player2, new WanderingOnes());
        enchanted.tap();

        Permanent free = addCreatureReady(player2, new WanderingOnes());
        free.tap();

        Permanent aura = new Permanent(new MysticRestraints());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToUpkeep(player2);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(free.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature untaps again once Mystic Restraints leaves the battlefield")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new WanderingOnes());
        creature.tap();

        Permanent aura = new Permanent(new MysticRestraints());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mystic Restraints can be cast during an opponent's turn because it has flash")
    void canBeCastDuringOpponentsTurn() {
        Permanent creature = addCreatureReady(player1, new WanderingOnes());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new MysticRestraints()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.passPriority(player2);
        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Mystic Restraints cannot target a noncreature permanent")
    void cannotTargetNonCreaturePermanent() {
        Permanent nonCreature = harness.addToBattlefieldAndReturn(player2, new CageOfHands());

        harness.setHand(player1, List.of(new MysticRestraints()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Mystic Restraints fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player2, new WanderingOnes());

        harness.setHand(player1, List.of(new MysticRestraints()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mystic Restraints");
        harness.assertNotOnBattlefield(player1, "Mystic Restraints");
    }
}
