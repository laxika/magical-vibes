package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DragonsClaw;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulBleed.class, RuneclawBear.class, DragonsClaw.class})
class SoulBleedTest extends BaseCardTest {

    @Test
    @DisplayName("Can target a creature with Soul Bleed")
    void canTargetCreature() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new SoulBleed()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Soul Bleed")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new RuneclawBear());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new DragonsClaw());

        harness.setHand(player1, List.of(new SoulBleed()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Resolving Soul Bleed attaches it to target creature")
    void resolvingAttachesToCreature() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());

        harness.setHand(player1, List.of(new SoulBleed()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Soul Bleed")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature's controller loses 1 life at their upkeep")
    void enchantedCreatureControllerLosesLifeAtUpkeep() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SoulBleed());
        auraPerm.setAttachedTo(creature.getId());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Life loss trigger does NOT fire during aura controller's upkeep")
    void lifeLossDoesNotFireDuringAuraControllerUpkeep() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SoulBleed());
        auraPerm.setAttachedTo(creature.getId());

        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Player1 (aura controller) should not lose life
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Life loss accumulates over multiple upkeeps")
    void lifeLossAccumulatesOverUpkeeps() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SoulBleed());
        auraPerm.setAttachedTo(creature.getId());

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("No life loss after Soul Bleed is removed")
    void noLifeLossAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new SoulBleed());
        auraPerm.setAttachedTo(creature.getId());

        // Remove Soul Bleed
        gd.playerBattlefields.get(player1.getId()).remove(auraPerm);

        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("A pending upkeep trigger still resolves after the Aura and creature leave")
    void pendingTriggerSurvivesRemovalOfAuraAndCreature() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SoulBleed());
        aura.setAttachedTo(creature.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player2);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerBattlefields.get(player2.getId()).remove(creature);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Enchanting your own creature makes you lose life during your upkeep")
    void enchantingOwnCreatureLosesLife() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new SoulBleed());
        aura.setAttachedTo(creature.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
    }

}
