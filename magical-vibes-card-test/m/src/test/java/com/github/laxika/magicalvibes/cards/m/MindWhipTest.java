package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.b.Betrayal;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MindWhip.class, BalduvianBears.class, Forest.class, Betrayal.class})
class MindWhipTest extends BaseCardTest {

    @Test
    @DisplayName("Can enchant a creature with Mind Whip")
    void canEnchantCreature() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());

        harness.setHand(player1, List.of(new MindWhip()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof MindWhip
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a non-creature permanent")
    void cannotEnchantNonCreature() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        addCreatureReady(player2, new BalduvianBears()); // legal target so the Aura is playable

        harness.setHand(player1, List.of(new MindWhip()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, land.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enchanted controller pays {3} — no damage, creature stays untapped")
    void paysToAvoidPenalty() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());
        attachMindWhip(creature);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passBothPriorities(); // resolve trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(creature.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.COLORLESS)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining payment deals 2 damage and taps the enchanted creature")
    void declineDamagesAndTaps() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());
        attachMindWhip(creature);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Accepting without enough mana applies the penalty")
    void cannotPayAppliesPenalty() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());
        attachMindWhip(creature);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true); // accepts but can't pay

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Does not trigger during the Aura controller's upkeep")
    void doesNotFireDuringAuraControllerUpkeep() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());
        attachMindWhip(creature);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore);
        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The trigger still taps the last enchanted creature if Mind Whip leaves before resolution")
    void triggerUsesLastEnchantedCreatureAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MindWhip());
        aura.setAttachedTo(creature.getId());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The penalty triggers abilities when the enchanted creature becomes tapped")
    @CardUsed({Betrayal.class})
    void penaltyTriggersBecomesTappedAbilities() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());
        attachMindWhip(creature);
        Permanent betrayal = harness.addToBattlefieldAndReturn(player1, new Betrayal());
        betrayal.setAttachedTo(creature.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));
        int handBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("The trigger still damages the upkeep player after the enchanted creature leaves")
    void triggerStillDamagesAfterCreatureLeaves() {
        Permanent creature = addCreatureReady(player2, new BalduvianBears());
        attachMindWhip(creature);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        advanceToUpkeep(player2);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Mind Whip can enchant its controller's creature and penalizes that controller")
    void ownCreatureControllerTakesPenalty() {
        Permanent creature = addCreatureReady(player1, new BalduvianBears());
        harness.setHand(player1, List.of(new MindWhip()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    private void attachMindWhip(Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new MindWhip());
        aura.setAttachedTo(creature.getId());
    }
}
