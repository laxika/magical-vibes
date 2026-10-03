package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NowhereToRun;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrystalCarapace.class, FountainOfYouth.class, GrizzlyBears.class, ProdigalPyromancer.class, Shock.class})
class CrystalCarapaceTest extends BaseCardTest {

    @Test
    @DisplayName("Crystal Carapace attaches to a creature and gives it +3/+3")
    void attachesAndBoostsEnchantedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CrystalCarapace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Crystal Carapace");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    @DisplayName("Crystal Carapace's ward counters an unpaid opponent spell")
    void wardCountersUnpaidSpell() {
        Permanent creature = enchantedCreature(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Paying Crystal Carapace's ward lets an opponent spell resolve")
    void payingWardLetsSpellResolve() {
        Permanent creature = enchantedCreature(player1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player2, 0, creature.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Cycling Crystal Carapace discards it and draws a card")
    void cyclingDrawsACard() {
        harness.setHand(player1, List.of(new CrystalCarapace()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Crystal Carapace");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Crystal Carapace cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        harness.setHand(player1, List.of(new CrystalCarapace()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Ward counters an opponent's targeted activated ability without removing its source")
    void wardCountersOpponentActivatedAbility() {
        Permanent creature = enchantedCreature(player1);
        Permanent pyromancer = addCreatureReady(player2, new ProdigalPyromancer());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, creature.getId());
        resolveAllTriggers();

        assertThat(creature.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(pyromancer);
        assertThat(pyromancer.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ward does not trigger for the enchanted creature's controller's spell")
    void ownSpellDoesNotTriggerWard() {
        Permanent creature = enchantedCreature(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ward belongs to the enchanted creature even when its controller does not control the Aura")
    void auraOnOpponentCreatureGrantsWardAgainstAuraController() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CrystalCarapace(), new Shock()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castEnchantment(player1, 0, creature.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Shock");
        assertThat(creature.getMarkedDamage()).isZero();
    }

    @Test
    @CardUsed({CrystalCarapace.class, GrizzlyBears.class, Shock.class, NowhereToRun.class})
    @DisplayName("Nowhere to Run suppresses the ward granted by Crystal Carapace")
    void nowhereToRunSuppressesGrantedWard() {
        Permanent creature = enchantedCreature(player1);
        harness.addToBattlefield(player2, new NowhereToRun());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, creature.getId());
        resolveAllTriggers();

        assertThat(creature.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cycling works during an opponent's turn and discards before drawing on resolution")
    void cyclingOnOpponentTurnDiscardsAsCost() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CrystalCarapace()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        harness.assertInGraveyard(player1, "Crystal Carapace");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        resolveAllTriggers();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cycling cannot be activated without two mana")
    void cyclingRequiresFullManaCost() {
        harness.setHand(player1, List.of(new CrystalCarapace()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Crystal Carapace");
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent enchantedCreature(Player player) {
        Permanent creature = addCreatureReady(player, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new CrystalCarapace());
        aura.setAttachedTo(creature.getId());
        return creature;
    }
}
