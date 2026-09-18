package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.ForgottenCave;
import com.github.laxika.magicalvibes.cards.p.Plateau;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EternalDragon.class, Plateau.class, ForgottenCave.class})
class EternalDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Plainscycling searches for a Plains card and puts it into hand")
    void plainscyclingSearchesForPlains() {
        EternalDragon dragon = new EternalDragon();
        Card plateau = new Plateau();
        Card cave = new ForgottenCave();
        harness.setHand(player1, List.of(dragon));
        harness.setLibrary(player1, List.of(plateau, cave));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(plateau);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(plateau.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(dragon.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(cave.getId()))
                .noneMatch(card -> card.getId().equals(plateau.getId()));
    }

    @Test
    @DisplayName("Plainscycling requires its full generic mana cost")
    void plainscyclingRequiresTwoGenericMana() {
        EternalDragon dragon = new EternalDragon();
        harness.setHand(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(dragon.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Plainscycling completes without a choice when no Plains is in the library")
    void plainscyclingCanFailToFind() {
        EternalDragon dragon = new EternalDragon();
        Card cave = new ForgottenCave();
        harness.setHand(player1, List.of(dragon));
        harness.setLibrary(player1, List.of(cave));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(dragon.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(cave);
    }

    @Test
    @DisplayName("The graveyard ability returns Eternal Dragon to hand during upkeep")
    void returnsFromGraveyardDuringUpkeep() {
        EternalDragon dragon = new EternalDragon();
        harness.setGraveyard(player1, List.of(dragon));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(dragon.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(dragon.getId()));
    }

    @Test
    @DisplayName("The graveyard ability returns only the activating Eternal Dragon")
    void returnsOnlyTheActivatingDragon() {
        EternalDragon otherDragon = new EternalDragon();
        Card cave = new ForgottenCave();
        EternalDragon dragon = new EternalDragon();
        harness.setGraveyard(player1, List.of(otherDragon, cave, dragon));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateGraveyardAbility(player1, 2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(dragon.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(otherDragon, cave);
    }

    @Test
    @DisplayName("The graveyard ability requires both white mana symbols")
    void graveyardAbilityRequiresTwoWhiteMana() {
        EternalDragon dragon = new EternalDragon();
        harness.setGraveyard(player1, List.of(dragon));
        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(dragon);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(dragon);
    }

    @Test
    @DisplayName("The graveyard ability cannot be activated outside upkeep")
    void cannotReturnFromGraveyardOutsideUpkeep() {
        EternalDragon dragon = new EternalDragon();
        harness.setGraveyard(player1, List.of(dragon));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("upkeep");
    }
}
