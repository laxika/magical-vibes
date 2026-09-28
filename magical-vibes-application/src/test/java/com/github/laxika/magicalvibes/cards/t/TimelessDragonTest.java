package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.ForgottenCave;
import com.github.laxika.magicalvibes.cards.p.Plateau;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({TimelessDragon.class, Plateau.class, ForgottenCave.class})
class TimelessDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Plainscycling searches for a Plains card and puts it into hand")
    void plainscyclingSearchesForPlains() {
        TimelessDragon dragon = new TimelessDragon();
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
                .contains(cave)
                .doesNotContain(plateau);
    }

    @Test
    @DisplayName("Plainscycling requires its full generic mana cost")
    void plainscyclingRequiresTwoGenericMana() {
        TimelessDragon dragon = new TimelessDragon();
        harness.setHand(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(dragon.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Eternalize creates a 4/4 black Zombie Dragon token copy with no mana cost")
    void eternalizeCreatesFourFourBlackZombieDragonToken() {
        TimelessDragon dragon = new TimelessDragon();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setGraveyard(player1, List.of(dragon));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Timeless Dragon") && p.getCard().isToken())
                .findFirst().orElseThrow();

        assertThat(token.getEffectivePower()).isEqualTo(4);
        assertThat(token.getEffectiveToughness()).isEqualTo(4);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes())
                .contains(CardSubtype.ZOMBIE, CardSubtype.DRAGON);
        assertThat(token.getCard().getManaCost()).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(dragon.getId()));
    }

    @Test
    @DisplayName("Eternalize can only be activated at sorcery speed")
    void eternalizeOnlyAtSorcerySpeed() {
        harness.setGraveyard(player1, List.of(new TimelessDragon()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInGraveyard(player1, "Timeless Dragon");
    }
}
