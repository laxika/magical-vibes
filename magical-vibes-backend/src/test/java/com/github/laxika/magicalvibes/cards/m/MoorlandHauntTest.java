package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.CreateCreatureTokenEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MoorlandHauntTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Moorland Haunt has correct activated abilities")
    void hasCorrectAbilities() {
        MoorlandHaunt card = new MoorlandHaunt();

        assertThat(card.getActivatedAbilities()).hasSize(2);

        // Ability 0: {T}: Add {C}.
        var manaAbility = card.getActivatedAbilities().get(0);
        assertThat(manaAbility.isRequiresTap()).isTrue();
        assertThat(manaAbility.getManaCost()).isNull();
        assertThat(manaAbility.isNeedsTarget()).isFalse();
        assertThat(manaAbility.getEffects()).hasSize(1);
        assertThat(manaAbility.getEffects().getFirst()).isInstanceOf(AwardManaEffect.class);

        // Ability 1: {W}{U}, {T}, Exile a creature card from your graveyard: Create a 1/1 white Spirit creature token with flying.
        var tokenAbility = card.getActivatedAbilities().get(1);
        assertThat(tokenAbility.isRequiresTap()).isTrue();
        assertThat(tokenAbility.getManaCost()).isEqualTo("{W}{U}");
        assertThat(tokenAbility.isNeedsTarget()).isFalse();
        assertThat(tokenAbility.getEffects()).hasSize(2);
        assertThat(tokenAbility.getEffects().get(0)).isInstanceOf(ExileCardFromGraveyardCost.class);
        ExileCardFromGraveyardCost exileCost = (ExileCardFromGraveyardCost) tokenAbility.getEffects().get(0);
        assertThat(exileCost.requiredType()).isEqualTo(CardType.CREATURE);
        assertThat(tokenAbility.getEffects().get(1)).isInstanceOf(CreateCreatureTokenEffect.class);

        CreateCreatureTokenEffect tokenEffect = (CreateCreatureTokenEffect) tokenAbility.getEffects().get(1);
        assertThat(tokenEffect.tokenName()).isEqualTo("Spirit");
        assertThat(tokenEffect.power()).isEqualTo(1);
        assertThat(tokenEffect.toughness()).isEqualTo(1);
        assertThat(tokenEffect.color()).isEqualTo(CardColor.WHITE);
        assertThat(tokenEffect.subtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(tokenEffect.keywords()).containsExactly(Keyword.FLYING);
    }

    // ===== Mana ability =====

    @Test
    @DisplayName("Tapping for colorless mana adds {C}")
    void tapForColorlessMana() {
        harness.addToBattlefield(player1, new MoorlandHaunt());

        harness.activateAbility(player1, 0, 0, null, null);

        Permanent land = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    // ===== Token ability: activation =====

    @Test
    @DisplayName("Token ability prompts for graveyard exile cost choice")
    void tokenAbilityPromptsForGraveyardExileCost() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.awaitingInputType())
                .isEqualTo(AwaitingInput.ACTIVATED_ABILITY_GRAVEYARD_EXILE_COST_CHOICE);
    }

    @Test
    @DisplayName("Activating token ability puts it on the stack after exile cost")
    void activatingPutsOnStack() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Moorland Haunt");
    }

    @Test
    @DisplayName("Creature card is exiled from graveyard as cost")
    void creatureCardExiledFromGraveyard() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Llanowar Elves"));
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));
    }

    @Test
    @DisplayName("Land is tapped as cost")
    void landIsTappedAsCost() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        Permanent land = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Moorland Haunt"))
                .findFirst().orElseThrow();
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating token ability")
    void manaIsConsumed() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        // 3 - 2 ({W}{U}) = 1 mana remaining
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    // ===== Token ability: resolution =====

    @Test
    @DisplayName("Resolving ability creates a 1/1 white Spirit token with flying")
    void resolvingCreatesSpiritToken() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spirit"))
                .findFirst().orElseThrow();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.SPIRIT);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Spirit token has summoning sickness")
    void tokenHasSummoningSickness() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Spirit"))
                .findFirst().orElseThrow();
        assertThat(token.isSummoningSick()).isTrue();
    }

    // ===== Validation =====

    @Test
    @DisplayName("Cannot activate token ability without creature card in graveyard")
    void cannotActivateWithoutCreatureInGraveyard() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot activate token ability with only non-creature cards in graveyard")
    void cannotActivateWithOnlyNonCreatureInGraveyard() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");
    }

    @Test
    @DisplayName("Cannot activate token ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Cannot activate token ability when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Tap for mana first
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Game log =====

    @Test
    @DisplayName("Creating Spirit token is logged")
    void tokenCreationIsLogged() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.gameLog).anyMatch(log -> log.contains("Spirit") && log.contains("token"));
    }
}
