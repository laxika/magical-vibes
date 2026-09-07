package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Seizures.class, BalduvianBears.class, Mountain.class, IcyManipulator.class})
class SeizuresTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant a creature with Seizures")
    void canEnchantCreature() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());

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
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());

        harness.setHand(player1, List.of(new Seizures()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted controller pays {3} — no damage")
    void paysToAvoidDamage() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());
        attachSeizures(creature);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player2, List.of(0));
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
        Permanent creature = addCreatureReady(player2, new BalduvianBears());
        attachSeizures(creature);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Accepting without enough mana applies the penalty")
    void cannotPayAppliesPenalty() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());
        attachSeizures(creature);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("An un-enchanted creature becoming tapped does not trigger")
    void unenchantedCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Triggers when another effect taps the enchanted creature")
    void triggersWhenAnotherEffectTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());
        attachSeizures(creature);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        activateIcyManipulator(creature);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
    }

    @Test
    @DisplayName("Does not trigger when an already-tapped enchanted creature is tapped again")
    void alreadyTappedCreatureDoesNotTrigger() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());
        attachSeizures(creature);
        creature.tap();
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        activateIcyManipulator(creature);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
    }

    private void attachSeizures(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Seizures());
        aura.setAttachedTo(creature.getId());
    }

    private void activateIcyManipulator(Permanent target) {
        Permanent manipulator = harness.addToBattlefieldAndReturn(player1, new IcyManipulator());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(manipulator), null, target.getId());
    }
}
