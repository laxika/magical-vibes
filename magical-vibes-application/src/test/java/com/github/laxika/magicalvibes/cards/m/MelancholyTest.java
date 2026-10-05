package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AetherMembrane;
import com.github.laxika.magicalvibes.cards.s.SealOfPrimordium;
import com.github.laxika.magicalvibes.cards.s.StonewoodInvocation;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Melancholy.class, AetherMembrane.class, SealOfPrimordium.class, StonewoodInvocation.class})
class MelancholyTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Melancholy taps the enchanted creature")
    void resolvingTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new AetherMembrane());

        harness.setHand(player1, List.of(new Melancholy()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new AetherMembrane());
        creature.tap();
        attachMelancholy(player1, creature);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature untaps again once Melancholy leaves the battlefield")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new AetherMembrane());
        creature.tap();
        Permanent aura = attachMelancholy(player1, creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to pay {B} sacrifices Melancholy")
    void decliningPaymentSacrificesAura() {
        Permanent creature = addCreatureReady(player2, new AetherMembrane());
        attachMelancholy(player1, creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Melancholy");
        harness.assertInGraveyard(player1, "Melancholy");
    }

    @Test
    @DisplayName("Paying {B} keeps Melancholy on the battlefield")
    void payingKeepsAura() {
        Permanent creature = addCreatureReady(player2, new AetherMembrane());
        attachMelancholy(player1, creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Melancholy");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent creature = addCreatureReady(player2, new AetherMembrane());
        attachMelancholy(player1, creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Melancholy");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Melancholy")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new AetherMembrane());
        Permanent nonCreaturePermanent = harness.addToBattlefieldAndReturn(player1, new SealOfPrimordium());
        harness.setHand(player1, List.of(new Melancholy()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, nonCreaturePermanent.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Enter trigger still taps the enchanted creature after it gains shroud")
    void enterTriggerDoesNotTargetEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new AetherMembrane());
        harness.setHand(player1, List.of(new Melancholy()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setHand(player2, List.of(new StonewoodInvocation()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Melancholy");

        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Melancholy");
    }

    @Test
    @DisplayName("Melancholy only prevents the enchanted creature from untapping")
    void otherCreaturesStillUntap() {
        Permanent enchanted = addCreatureReady(player2, new AetherMembrane());
        Permanent other = addCreatureReady(player2, new AetherMembrane());
        enchanted.tap();
        other.tap();
        attachMelancholy(player1, enchanted);

        harness.performUntapStep(player2);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Controller may decline upkeep payment even when black mana is available")
    void canDeclineAffordablePayment() {
        Permanent creature = addCreatureReady(player2, new AetherMembrane());
        attachMelancholy(player1, creature);
        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Melancholy");
        harness.assertInGraveyard(player1, "Melancholy");
        harness.assertOnBattlefield(player2, "Aether Membrane");
    }

    private Permanent attachMelancholy(Player auraController, Permanent enchanted) {
        Permanent aura = harness.addToBattlefieldAndReturn(auraController, new Melancholy());
        aura.setAttachedTo(enchanted.getId());
        return aura;
    }

}
