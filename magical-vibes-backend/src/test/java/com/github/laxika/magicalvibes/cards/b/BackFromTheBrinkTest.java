package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ActivationTimingRestriction;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfExiledCostCardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BackFromTheBrinkTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has activated ability with exile creature from graveyard cost and token copy effect")
    void hasCorrectAbility() {
        BackFromTheBrink card = new BackFromTheBrink();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.getManaCost()).isNull();
        assertThat(ability.isRequiresTap()).isFalse();
        assertThat(ability.getTimingRestriction()).isEqualTo(ActivationTimingRestriction.SORCERY_SPEED);
        assertThat(ability.getEffects()).hasSize(2);
        assertThat(ability.getEffects().get(0)).isInstanceOf(ExileCardFromGraveyardCost.class);
        ExileCardFromGraveyardCost exileCost = (ExileCardFromGraveyardCost) ability.getEffects().get(0);
        assertThat(exileCost.requiredType()).isEqualTo(CardType.CREATURE);
        assertThat(exileCost.payExiledCardManaCost()).isTrue();
        assertThat(exileCost.imprintOnSource()).isTrue();
        assertThat(ability.getEffects().get(1)).isInstanceOf(CreateTokenCopyOfExiledCostCardEffect.class);
    }

    // ===== Ability activation =====

    @Test
    @DisplayName("Prompts for graveyard exile cost choice when creature in graveyard")
    void promptsForGraveyardExileCost() {
        harness.addToBattlefield(player1, new BackFromTheBrink());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.interaction.awaitingInputType())
                .isEqualTo(AwaitingInput.ACTIVATED_ABILITY_GRAVEYARD_EXILE_COST_CHOICE);
    }

    @Test
    @DisplayName("Creates token copy of exiled creature after paying its mana cost")
    void createsTokenCopyOfExiledCreature() {
        harness.addToBattlefield(player1, new BackFromTheBrink());
        // Llanowar Elves costs {G}
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        // Creature card should be exiled from graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

        // Resolve the ability
        harness.passBothPriorities();

        // Token copy of Llanowar Elves should be on the battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves") && p.getCard().isToken());
    }

    @Test
    @DisplayName("Pays the exiled creature's mana cost — not free")
    void paysExiledCreatureManaCost() {
        harness.addToBattlefield(player1, new BackFromTheBrink());
        // Grizzly Bears costs {1}{G}
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        // Mana should be fully consumed
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);
        // Token copy should be on battlefield
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken());
    }

    @Test
    @DisplayName("Fails without enough mana to pay exiled creature's cost")
    void failsWithoutEnoughMana() {
        harness.addToBattlefield(player1, new BackFromTheBrink());
        // Grizzly Bears costs {1}{G}, only provide 1 colorless
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        assertThatThrownBy(() -> harness.handleGraveyardCardChosen(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Fails without creature card in graveyard")
    void failsWithoutCreatureInGraveyard() {
        harness.addToBattlefield(player1, new BackFromTheBrink());
        harness.setGraveyard(player1, List.of());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Only creature cards qualify — non-creature cards are ignored")
    void onlyCreatureCardsQualify() {
        harness.addToBattlefield(player1, new BackFromTheBrink());
        // Only non-creature cards in graveyard
        harness.setGraveyard(player1, List.of(new Shock()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Can only be activated at sorcery speed")
    void sorcerySpeedOnly() {
        harness.addToBattlefield(player1, new BackFromTheBrink());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        // Move to opponent's turn — can't activate at sorcery speed
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Token copy has correct characteristics — name, power/toughness, types")
    void tokenCopyHasCorrectCharacteristics() {
        harness.addToBattlefield(player1, new BackFromTheBrink());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        var token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken())
                .findFirst().orElseThrow();

        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
    }

    @Test
    @DisplayName("Can activate multiple times with different creatures")
    void canActivateMultipleTimes() {
        harness.addToBattlefield(player1, new BackFromTheBrink());
        harness.setGraveyard(player1, List.of(new LlanowarElves(), new GrizzlyBears()));

        // First activation — exile Llanowar Elves ({G})
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Llanowar Elves") && p.getCard().isToken());

        // Second activation — exile Grizzly Bears ({1}{G}), now at index 0 since Elves was removed
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears") && p.getCard().isToken());
    }
}
