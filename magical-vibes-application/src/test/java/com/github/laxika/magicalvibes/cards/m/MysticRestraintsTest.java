package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MysticRestraintsTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Mystic Restraints taps the enchanted creature and attaches")
    void resolvingTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

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
        Permanent enchanted = addCreatureReady(player2, new GrizzlyBears());
        enchanted.tap();

        Permanent free = addCreatureReady(player2, new GrizzlyBears());
        free.tap();

        Permanent aura = new Permanent(new MysticRestraints());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);

        advanceToNextTurn(player1);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(free.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Creature untaps again once Mystic Restraints leaves the battlefield")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        Permanent aura = new Permanent(new MysticRestraints());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mystic Restraints fizzles if its target leaves before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new MysticRestraints()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        gd.playerBattlefields.get(player2.getId()).clear();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mystic Restraints");
        harness.assertNotOnBattlefield(player1, "Mystic Restraints");
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // END_STEP -> CLEANUP
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // CLEANUP -> next turn
    }
}
