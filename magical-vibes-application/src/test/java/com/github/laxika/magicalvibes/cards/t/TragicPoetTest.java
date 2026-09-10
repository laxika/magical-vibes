package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AuraFlux;
import com.github.laxika.magicalvibes.cards.d.DefenseOfTheHeart;
import com.github.laxika.magicalvibes.cards.p.PlagueBeetle;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({TragicPoet.class, AuraFlux.class, DefenseOfTheHeart.class, PlagueBeetle.class})
class TragicPoetTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability sacrifices Tragic Poet and puts ability on the stack")
    void activatingAbilitySacrificesAndPutsOnStack() {
        Card enchantment = new AuraFlux();
        addCreatureReady(player1, new TragicPoet());
        harness.setGraveyard(player1, List.of(enchantment));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(enchantment.getId()));

        harness.assertNotOnBattlefield(player1, "Tragic Poet");
        harness.assertInGraveyard(player1, "Tragic Poet");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Returns the targeted enchantment from the graveyard to hand")
    void returnsTargetedEnchantmentFromGraveyardToHand() {
        Card enchantment = new AuraFlux();
        addCreatureReady(player1, new TragicPoet());
        harness.setGraveyard(player1, List.of(enchantment));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(enchantment.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Aura Flux");
        harness.assertNotInGraveyard(player1, "Aura Flux");
    }

    @Test
    @DisplayName("Targets a specific enchantment when multiple are in the graveyard")
    void targetsSpecificEnchantmentFromGraveyard() {
        Card auraFlux = new AuraFlux();
        Card defenseOfTheHeart = new DefenseOfTheHeart();
        addCreatureReady(player1, new TragicPoet());
        harness.setGraveyard(player1, List.of(auraFlux, defenseOfTheHeart));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(defenseOfTheHeart.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Defense of the Heart");
        harness.assertInGraveyard(player1, "Aura Flux");
    }

    @Test
    @DisplayName("Cannot target a non-enchantment card in the graveyard")
    void cannotTargetNonEnchantmentFromGraveyard() {
        Card creature = new PlagueBeetle();
        Card enchantment = new AuraFlux();
        addCreatureReady(player1, new TragicPoet());
        harness.setGraveyard(player1, List.of(creature, enchantment));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creature.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target card");

        harness.assertOnBattlefield(player1, "Tragic Poet");
    }

    @Test
    @DisplayName("Cannot activate without a legal enchantment target")
    void cannotActivateWithoutLegalEnchantmentTarget() {
        addCreatureReady(player1, new TragicPoet());
        harness.setGraveyard(player1, List.of());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("target");

        harness.assertOnBattlefield(player1, "Tragic Poet");
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent poet = addCreatureReady(player1, new TragicPoet());
        poet.tap();
        Card enchantment = new AuraFlux();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(enchantment.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new TragicPoet());
        Card enchantment = new AuraFlux();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(enchantment.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot target an enchantment in an opponent's graveyard")
    void cannotTargetOpponentsGraveyard() {
        Card enchantment = new AuraFlux();
        addCreatureReady(player1, new TragicPoet());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(enchantment));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(enchantment.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");

        harness.assertOnBattlefield(player1, "Tragic Poet");
    }

    @Test
    @DisplayName("Fizzling does not choose a replacement after the target leaves the graveyard")
    void fizzlesWhenTargetLeavesGraveyard() {
        Card originalTarget = new AuraFlux();
        Card replacement = new DefenseOfTheHeart();
        addCreatureReady(player1, new TragicPoet());
        harness.setGraveyard(player1, List.of(originalTarget));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(originalTarget.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(originalTarget);
        gd.playerGraveyards.get(player1.getId()).add(replacement);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Defense of the Heart");
        harness.assertInGraveyard(player1, "Defense of the Heart");
    }

    @Test
    @DisplayName("Stack is empty after full resolution")
    void stackIsEmptyAfterResolution() {
        Card enchantment = new AuraFlux();
        addCreatureReady(player1, new TragicPoet());
        harness.setGraveyard(player1, List.of(enchantment));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(enchantment.getId()));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }
}
