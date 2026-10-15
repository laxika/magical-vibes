package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BayFalcon;
import com.github.laxika.magicalvibes.cards.c.CursedTotem;
import com.github.laxika.magicalvibes.cards.e.EnchantmentAlteration;
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

@CardUsed({Thirst.class, BayFalcon.class, CursedTotem.class, EnchantmentAlteration.class})
class ThirstTest extends BaseCardTest {

    private Permanent attachThirst(Player auraController, Permanent enchanted) {
        Permanent auraPerm = harness.addToBattlefieldAndReturn(auraController, new Thirst());
        auraPerm.setAttachedTo(enchanted.getId());
        return auraPerm;
    }

    @Test
    @DisplayName("Resolving Thirst taps the enchanted creature")
    void resolvingTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new BayFalcon());

        harness.setHand(player1, List.of(new Thirst()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities(); // resolve enchantment spell
        harness.passBothPriorities(); // resolve ETB tap trigger

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new BayFalcon());
        creature.tap();
        attachThirst(player1, creature);

        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creature untaps again once Thirst leaves the battlefield")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new BayFalcon());
        creature.tap();
        Permanent aura = attachThirst(player1, creature);

        gd.playerBattlefields.get(player1.getId()).remove(aura);
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining to pay {U} sacrifices Thirst")
    void decliningPaymentSacrificesAura() {
        Permanent creature = addCreatureReady(player2, new BayFalcon());
        attachThirst(player1, creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger -> may-pay prompt

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Thirst");
        harness.assertInGraveyard(player1, "Thirst");
    }

    @Test
    @DisplayName("Paying {U} keeps Thirst on the battlefield")
    void payingKeepsAura() {
        Permanent creature = addCreatureReady(player2, new BayFalcon());
        attachThirst(player1, creature);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve upkeep trigger -> may-pay prompt
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Thirst");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentUpkeep() {
        Permanent creature = addCreatureReady(player2, new BayFalcon());
        attachThirst(player1, creature);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Thirst");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent with Thirst")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new CursedTotem());
        harness.setHand(player1, List.of(new Thirst()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @CardUsed({Thirst.class, BayFalcon.class, EnchantmentAlteration.class})
    @DisplayName("The enter trigger taps the current enchanted creature after Thirst moves")
    void enterTriggerFollowsMovedAura() {
        Permanent original = addCreatureReady(player2, new BayFalcon());
        Permanent destination = addCreatureReady(player2, new BayFalcon());
        harness.setHand(player1, List.of(new Thirst(), new EnchantmentAlteration()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castEnchantment(player1, 0, original.getId());
        harness.passBothPriorities();
        Permanent aura = findPermanent(player1, "Thirst");
        assertThat(original.isTapped()).isFalse();

        harness.castInstant(player1, 0, aura.getId());
        harness.passBothPriorities();
        assertThat(aura.getAttachedTo()).isEqualTo(destination.getId());
        harness.passBothPriorities();

        assertThat(destination.isTapped()).isTrue();
        assertThat(original.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Thirst prevents only the enchanted creature from untapping")
    void otherCreaturesStillUntap() {
        Permanent enchanted = addCreatureReady(player2, new BayFalcon());
        Permanent other = addCreatureReady(player2, new BayFalcon());
        enchanted.tap();
        other.tap();
        attachThirst(player1, enchanted);

        harness.performUntapStep(player2);

        assertThat(enchanted.isTapped()).isTrue();
        assertThat(other.isTapped()).isFalse();
    }
}
