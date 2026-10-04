package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AshcoatBear;
import com.github.laxika.magicalvibes.cards.p.PrismaticLens;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({FallenIdeal.class, AshcoatBear.class, PrismaticLens.class})
class FallenIdealTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Fallen Ideal attaches it to the target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new FallenIdeal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castEnchantment(player1, 0, bear.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Fallen Ideal");
        assertThat(aura.getAttachedTo()).isEqualTo(bear.getId());
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature has flying")
    void grantsFlying() {
        Permanent bears = attachAuraToBears();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature can sacrifice a creature to get +2/+1")
    void grantedAbilityBoostsEnchantedCreature() {
        Permanent bears = attachAuraToBears();
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        harness.assertInGraveyard(player1, "Ashcoat Bear");
    }

    @Test
    @DisplayName("The granted pump wears off at end of turn")
    void grantedPumpWearsOffAtEndOfTurn() {
        Permanent bears = attachAuraToBears();
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The granted sacrifice ability only offers creatures as sacrifice choices")
    void sacrificeCostOnlyAllowsCreatures() {
        Permanent bears = attachAuraToBears();
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PrismaticLens());

        harness.activateAbility(player1, 0, null, null);

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .contains(fodder.getId(), bears.getId())
                .doesNotContain(artifact.getId());

        harness.handlePermanentChosen(player1, fodder.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("The enchanted creature's controller can activate the granted ability")
    void enchantedCreatureControllerCanActivateGrantedAbility() {
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FallenIdeal());
        aura.setAttachedTo(opponentBears.getId());
        Permanent fodder = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());

        int opponentBearsIndex = gd.playerBattlefields.get(player2.getId()).indexOf(opponentBears);
        harness.activateAbility(player2, opponentBearsIndex, null, null);
        harness.handlePermanentChosen(player2, fodder.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(3);
        harness.assertInGraveyard(player2, "Ashcoat Bear");
    }

    @Test
    @DisplayName("Fallen Ideal returns to its owner's hand from the graveyard")
    void returnsToOwnersHandWhenPutIntoGraveyard() {
        attachAuraToBears();
        Permanent aura = findPermanent(player1, "Fallen Ideal");

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fallen Ideal");
        harness.assertNotInGraveyard(player1, "Fallen Ideal");
        harness.assertNotOnBattlefield(player1, "Fallen Ideal");
    }

    @Test
    @DisplayName("Fallen Ideal cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player1,
                new PrismaticLens());
        harness.setHand(player1, List.of(new FallenIdeal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("A stolen Fallen Ideal returns to its owner's hand")
    void stolenAuraReturnsToOwnersHand() {
        Permanent bear = harness.addToBattlefieldAndReturn(player2, new AshcoatBear());
        FallenIdeal card = new FallenIdeal();
        card.setOwnerId(player1.getId());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, card);
        aura.setAttachedTo(bear.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));

        harness.assertInGraveyard(player1, "Fallen Ideal");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Fallen Ideal");
        harness.assertNotInHand(player2, "Fallen Ideal");
        harness.assertNotInGraveyard(player1, "Fallen Ideal");
    }

    @Test
    @DisplayName("The enchanted creature can sacrifice itself and the Aura returns")
    void enchantedCreatureCanSacrificeItself() {
        Permanent bear = attachAuraToBears();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertInGraveyard(player1, "Ashcoat Bear");
        harness.assertNotOnBattlefield(player1, "Ashcoat Bear");
        harness.assertNotOnBattlefield(player1, "Fallen Ideal");
        harness.assertInHand(player1, "Fallen Ideal");
        harness.assertNotInGraveyard(player1, "Fallen Ideal");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An activated pump still resolves after Fallen Ideal leaves the battlefield")
    void pumpResolvesAfterAuraLeaves() {
        Permanent bear = attachAuraToBears();
        Permanent fodder = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        Permanent aura = findPermanent(player1, "Fallen Ideal");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, fodder.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, aura));
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bear, Keyword.FLYING)).isFalse();
        harness.assertInHand(player1, "Fallen Ideal");
    }

    @Test
    @DisplayName("An Aura spell whose creature target disappears does not return from the graveyard")
    void illegalSpellTargetDoesNotTriggerReturn() {
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new AshcoatBear());
        harness.setHand(player1, List.of(new FallenIdeal()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, bear.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, bear));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Fallen Ideal");
        harness.assertNotInHand(player1, "Fallen Ideal");
        harness.assertNotOnBattlefield(player1, "Fallen Ideal");
        assertThat(gd.stack).isEmpty();
    }

    private Permanent attachAuraToBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1,
                new AshcoatBear());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new FallenIdeal());
        aura.setAttachedTo(bears.getId());
        return bears;
    }
}
