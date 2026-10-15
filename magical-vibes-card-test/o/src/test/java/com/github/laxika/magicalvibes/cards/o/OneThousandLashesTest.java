package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.d.DiscipleOfTheOldWays;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OneThousandLashes.class, DiscipleOfTheOldWays.class, PropheticPrism.class})
class OneThousandLashesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving attaches the Aura to the target creature")
    void resolvingAttachesToTarget() {
        Permanent creature = addCreatureReady(player2, new DiscipleOfTheOldWays());

        harness.setHand(player1, List.of(new OneThousandLashes()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("One Thousand Lashes")
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent creature = addCreatureReady(player1, new DiscipleOfTheOldWays());
        attachLashes(player2, creature);

        assertThatThrownBy(() -> declareAttackers(player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blocker = addCreatureReady(player2, new DiscipleOfTheOldWays());
        attachLashes(player1, blocker);

        Permanent attacker = addCreatureReady(player1, new DiscipleOfTheOldWays());
        attacker.setAttacking(true);

        prepareDeclareBlockers(player1);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot activate its abilities")
    void enchantedCreatureCannotActivateAbilities() {
        Permanent creature = addCreatureReady(player1, new DiscipleOfTheOldWays());
        attachLashes(player2, creature);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
    }

    @Test
    @DisplayName("Enchanted creature's controller loses 1 life at their upkeep")
    void controllerLosesOneLifeAtUpkeep() {
        Permanent creature = addCreatureReady(player2, new DiscipleOfTheOldWays());
        attachLashes(player1, creature);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("No life loss during the Aura controller's upkeep")
    void noLifeLossDuringAuraControllerUpkeep() {
        Permanent creature = addCreatureReady(player2, new DiscipleOfTheOldWays());
        attachLashes(player1, creature);

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());

        harness.setHand(player1, List.of(new OneThousandLashes()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Upkeep life loss still affects the original player after the creature changes controller")
    void upkeepLifeLossKeepsOriginalPlayer() {
        Permanent creature = addCreatureReady(player2, new DiscipleOfTheOldWays());
        attachLashes(player1, creature);

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        gd.playerBattlefields.get(player1.getId()).add(creature);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Removing the Aura does not stop its already triggered upkeep life loss")
    void upkeepLifeLossResolvesAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player2, new DiscipleOfTheOldWays());
        attachLashes(player1, creature);

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player2, 20);
        Permanent aura = findPermanent(player1, "One Thousand Lashes");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    private void attachLashes(com.github.laxika.magicalvibes.model.Player controller, Permanent enchanted) {
        Permanent aura = new Permanent(new OneThousandLashes());
        aura.setAttachedTo(enchanted.getId());
        gd.playerBattlefields.get(controller.getId()).add(aura);
    }
}
