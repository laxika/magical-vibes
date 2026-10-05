package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KulrathZealot.class, Forest.class, Island.class, Plains.class})
class KulrathZealotTest extends BaseCardTest {

    @Test
    @DisplayName("ETB exiles the top card with permission to play it until the end of your next turn")
    void etbExilesTopCardWithNextTurnPlayPermission() {
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new KulrathZealot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest(), new Forest()));
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).containsKey(topCard.getId());
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.exilePlayPermissions).containsKey(topCard.getId());
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.exilePlayPermissions).doesNotContainKey(topCard.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Basic landcycling discards the card and offers only basic lands")
    void basicLandcyclingDiscardsAndSearchesForBasicLand() {
        harness.setHand(player1, List.of(new KulrathZealot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new KulrathZealot()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Kulrath Zealot");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC))
                .hasSize(3);
    }

    @Test
    @DisplayName("Basic landcycling puts the chosen basic land into hand")
    void basicLandcyclingChoosesBasicLand() {
        harness.setHand(player1, List.of(new KulrathZealot()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new KulrathZealot()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        String chosenName = offered.getFirst().getName();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals(chosenName));
    }

    @Test
    void exiledLandCanBePlayed() {
        Card land = new Island();
        harness.setLibrary(player1, List.of(land));
        harness.enterBattlefieldAndReturn(player1, new KulrathZealot());
        harness.passBothPriorities();

        harness.castFromExile(player1, land.getId());

        harness.assertOnBattlefield(player1, "Island");
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(land);
    }

    @Test
    void exiledCreatureCanBeCastWithItsNormalManaCost() {
        Card creature = new KulrathZealot();
        harness.setLibrary(player1, List.of(creature));
        harness.enterBattlefieldAndReturn(player1, new KulrathZealot());
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFromExile(player1, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(creature.getId()));
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(creature);
    }

    @Test
    void basicLandcyclingMayFailToFindEvenWhenBasicLandIsAvailable() {
        Card land = new Forest();
        harness.setHand(player1, List.of(new KulrathZealot()));
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateHandAbility(player1, 0, null);
        harness.assertInGraveyard(player1, "Kulrath Zealot");
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
