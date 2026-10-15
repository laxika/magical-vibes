package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CentaurHealer;
import com.github.laxika.magicalvibes.cards.c.CodexShredder;
import com.github.laxika.magicalvibes.cards.d.DrudgeBeetle;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StabWound.class, CentaurHealer.class, DrudgeBeetle.class, CodexShredder.class})
class StabWoundTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        // A legal creature target must exist so the aura is playable; the cast then fails on the
        // illegal artifact target.
        harness.addToBattlefield(player2, new DrudgeBeetle());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CodexShredder());

        harness.setHand(player1, List.of(new StabWound()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted creature gets -2/-2")
    void enchantedCreatureGetsMinusTwoMinusTwo() {
        Permanent creature = addCreatureReady(player2, new CentaurHealer());

        harness.setHand(player1, List.of(new StabWound()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(1);
    }

    @Test
    @DisplayName("A 2/2 dies to the -2/-2")
    void twoTwoDies() {
        Permanent creature = addCreatureReady(player2, new DrudgeBeetle());

        harness.setHand(player1, List.of(new StabWound()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature's controller loses 2 life at their upkeep")
    void controllerLosesTwoLifeAtUpkeep() {
        Permanent creature = addCreatureReady(player2, new CentaurHealer());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new StabWound());
        auraPerm.setAttachedTo(creature.getId());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("No life loss during the Aura controller's upkeep")
    void noLifeLossDuringAuraControllerUpkeep() {
        Permanent creature = addCreatureReady(player2, new CentaurHealer());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new StabWound());
        auraPerm.setAttachedTo(creature.getId());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("No life loss after Stab Wound leaves the battlefield")
    void noLifeLossAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new CentaurHealer());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new StabWound());
        auraPerm.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Removing the Aura after its upkeep ability triggers does not stop life loss")
    void lifeLossResolvesAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player2, new CentaurHealer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StabWound());
        aura.setAttachedTo(creature.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);
    }

    @Test
    @DisplayName("Removing the creature after the upkeep trigger does not stop life loss")
    void lifeLossResolvesAfterCreatureLeaves() {
        Permanent creature = addCreatureReady(player2, new CentaurHealer());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new StabWound());
        aura.setAttachedTo(creature.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Stab Wound");
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Enchanting your own creature causes you to lose life during your upkeep")
    void ownCreatureControllerLosesLife() {
        Permanent creature = addCreatureReady(player1, new CentaurHealer());
        harness.setHand(player1, List.of(new StabWound()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }
}
