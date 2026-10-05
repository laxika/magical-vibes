package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PiousInterdiction.class, RaptorCompanion.class, PryingBlade.class})
class PiousInterdictionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Pious Interdiction puts it on the stack")
    void castingPutsOnStack() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new PiousInterdiction()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ENCHANTMENT_SPELL);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(PiousInterdiction.class);
    }

    @Test
    @DisplayName("Resolving Pious Interdiction attaches it to target creature")
    void resolvingAttachesToTarget() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new PiousInterdiction()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities(); // resolve aura spell

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Pious Interdiction")
                        && p.isAttached()
                        && p.getAttachedTo().equals(bearsPerm.getId()));
    }

    @Test
    @DisplayName("ETB trigger causes controller to gain 2 life")
    void etbGainsLife() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new PiousInterdiction()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities(); // resolve aura spell — ETB trigger goes on stack
        harness.passBothPriorities(); // resolve ETB trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB life gain works with non-default life totals")
    void etbGainsLifeWithCustomTotals() {
        harness.setLife(player1, 5);

        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new PiousInterdiction()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());
        harness.passBothPriorities(); // resolve aura
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(7);
    }

    @Test
    @DisplayName("Enchanted creature cannot attack")
    void enchantedCreatureCannotAttack() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        bearsPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player2, new PiousInterdiction());
        auraPerm.setAttachedTo(bearsPerm.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    @DisplayName("Enchanted creature cannot block")
    void enchantedCreatureCannotBlock() {
        Permanent blockerPerm = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        blockerPerm.setSummoningSick(false);

        Permanent auraPerm = harness.addToBattlefieldAndReturn(player1, new PiousInterdiction());
        auraPerm.setAttachedTo(blockerPerm.getId());

        Permanent atkPerm = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        atkPerm.setSummoningSick(false);
        atkPerm.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new com.github.laxika.magicalvibes.networking.message.BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid blocker index");
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player2, new RaptorCompanion());
        harness.addToBattlefield(player1, new PryingBlade());
        harness.setHand(player1, List.of(new PiousInterdiction()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        Permanent artifact = findPermanent(player1, "Prying Blade");

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Fizzles to graveyard if target creature is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent bearsPerm = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        bearsPerm.setSummoningSick(false);

        harness.setHand(player1, List.of(new PiousInterdiction()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, bearsPerm.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Pious Interdiction");
        harness.assertNotOnBattlefield(player1, "Pious Interdiction");
        harness.assertLife(player1, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Life gain uses the stack and survives the Aura leaving")
    void lifeGainSurvivesAuraLeaving() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new RaptorCompanion());
        harness.setHand(player1, List.of(new PiousInterdiction()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(gd.stack).hasSize(1);
        Permanent aura = findPermanent(player1, "Pious Interdiction");
        gd.playerBattlefields.get(player1.getId()).remove(aura);
        gd.playerGraveyards.get(player1.getId()).add(aura.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can enchant its controller's creature and gain life")
    void canEnchantOwnCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new RaptorCompanion());
        harness.setHand(player1, List.of(new PiousInterdiction()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Pious Interdiction").getAttachedTo()).isEqualTo(creature.getId());
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 20);
    }
}
