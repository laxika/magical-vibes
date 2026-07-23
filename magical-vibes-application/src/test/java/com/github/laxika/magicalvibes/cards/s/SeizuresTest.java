package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeizuresTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant a creature with Seizures")
    void canEnchantCreature() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Seizures()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Seizures")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        Permanent land = addLand(player2);
        addCreatureReady(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new Seizures()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted controller pays {3} — no damage")
    void paysToAvoidDamage() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachSeizures(creature);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttack(player2, 0);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining payment deals 3 damage to the enchanted creature's controller")
    void declineDamagesController() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachSeizures(creature);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttack(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Accepting without enough mana applies the penalty")
    void cannotPayAppliesPenalty() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachSeizures(creature);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttack(player2, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("An un-enchanted creature becoming tapped does not trigger")
    void unenchantedCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttack(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    private void attachSeizures(Permanent creature) {
        Permanent aura = new Permanent(new Seizures());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
    }

    private Permanent addLand(Player player) {
        Permanent land = new Permanent(new Mountain());
        gd.playerBattlefields.get(player.getId()).add(land);
        return land;
    }

    private void declareAttack(Player attacker, int creatureIndex) {
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, attacker, List.of(creatureIndex));
    }
}
