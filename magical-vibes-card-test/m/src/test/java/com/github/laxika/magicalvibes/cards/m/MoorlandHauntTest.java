package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.d.DreamTwist;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MoorlandHaunt.class, DoomedTraveler.class, DreamTwist.class})
class MoorlandHauntTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping for colorless mana adds {C}")
    void tapForColorlessMana() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MoorlandHaunt());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(land.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    @DisplayName("Token ability prompts for graveyard exile cost choice")
    void tokenAbilityPromptsForGraveyardExileCost() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
    }

    @Test
    @DisplayName("Activating token ability puts it on the stack after exile cost")
    void activatingPutsOnStack() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new DoomedTraveler()));
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
        harness.setGraveyard(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Doomed Traveler");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Doomed Traveler"));
    }

    @Test
    @DisplayName("Land is tapped as cost")
    void landIsTappedAsCost() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana is consumed when activating token ability")
    void manaIsConsumed() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        // 3 - 2 ({W}{U}) = 1 mana remaining
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Resolving ability creates a 1/1 white Spirit token with flying")
    void resolvingCreatesSpiritToken() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = findPermanent(player1, "Spirit");
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
        harness.setGraveyard(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Spirit");
        assertThat(token.isSummoningSick()).isTrue();
    }

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
        harness.setGraveyard(player1, List.of(new DreamTwist()));
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
        harness.setGraveyard(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("Cannot activate token ability when already tapped")
    void cannotActivateWhenTapped() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        // Tap for mana first
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("Only creature cards can be selected from a mixed graveyard")
    void choosesCreatureFromMixedGraveyard() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        DoomedTraveler creature = new DoomedTraveler();
        DreamTwist instant = new DreamTwist();
        harness.setGraveyard(player1, List.of(instant, creature));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);

        PendingInteraction.GraveyardExileCostChoice choice =
                (PendingInteraction.GraveyardExileCostChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIndices()).containsExactly(1);
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(creature);
        assertThat(countPermanents(player1, "Spirit")).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        assertThat(countPermanents(player2, "Spirit")).isZero();
    }

    @Test
    @DisplayName("An opponent's creature card cannot pay the exile cost")
    void cannotUseOpponentsGraveyard() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature");

        assertThat(land.isTapped()).isFalse();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
        harness.assertInGraveyard(player2, "Doomed Traveler");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The token ability resolves after Moorland Haunt leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);

        gd.playerBattlefields.get(player1.getId()).remove(land);
        gd.playerGraveyards.get(player1.getId()).add(land.getCard());
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Spirit")).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Moorland Haunt");
        harness.assertInGraveyard(player1, "Moorland Haunt");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creating Spirit token is logged")
    void tokenCreationIsLogged() {
        harness.addToBattlefield(player1, new MoorlandHaunt());
        harness.setGraveyard(player1, List.of(new DoomedTraveler()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("Spirit") && log.contains("token"));
    }
}
