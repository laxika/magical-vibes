package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SleepingPotionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sleeping Potion taps the enchanted creature")
    void resolvingTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new SleepingPotion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sleeping Potion")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        Permanent potion = attachPotion(player1, creature);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature untaps after Sleeping Potion leaves the battlefield")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        creature.tap();

        Permanent potion = attachPotion(player1, creature);
        gd.playerBattlefields.get(player1.getId()).remove(potion);

        advanceToNextTurn(player1);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sleeping Potion is sacrificed when the enchanted creature becomes the target of a spell")
    void sacrificedWhenEnchantedCreatureTargetedBySpell() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachPotion(player1, creature);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sleeping Potion");
        harness.assertInGraveyard(player1, "Sleeping Potion");
    }

    @Test
    @DisplayName("Sleeping Potion is not sacrificed when the enchanted creature becomes the target of an ability")
    void notSacrificedWhenEnchantedCreatureTargetedByAbility() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachPotion(player1, creature);

        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent icyManipulator = findPermanent(player1, "Icy Manipulator");
        icyManipulator.setSummoningSick(false);

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(icyManipulator), null, creature.getId());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Sleeping Potion");
    }

    private Permanent attachPotion(Player controller, Permanent creature) {
        Permanent potion = new Permanent(new SleepingPotion());
        potion.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(potion);
        return potion;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        harness.forceActivePlayer(currentActivePlayer);
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
