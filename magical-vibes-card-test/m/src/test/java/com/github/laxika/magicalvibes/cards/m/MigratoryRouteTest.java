package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CommandTower;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MigratoryRoute.class, Forest.class, CommandTower.class})
class MigratoryRouteTest extends BaseCardTest {

    @Test
    void createsFourFlyingBirdTokens() {
        harness.setHand(player1, List.of(new MigratoryRoute()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(findPermanents(player1, "Bird")).hasSize(4).allSatisfy(bird -> {
            assertThat(bird.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(bird.getCard().getSubtypes()).contains(CardSubtype.BIRD);
            assertThat(bird.getCard().getKeywords()).contains(Keyword.FLYING);
            assertThat(bird.getEffectivePower()).isEqualTo(1);
            assertThat(bird.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    void basicLandcyclingSearchesForBasicLand() {
        MigratoryRoute route = new MigratoryRoute();
        Forest forest = new Forest();
        harness.setHand(player1, List.of(route));
        harness.setLibrary(player1, List.of(forest, new MigratoryRoute(), new CommandTower()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(route);
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Forest");
    }

    @Test
    void basicLandcyclingPutsOnlyTheChosenLandIntoHand() {
        MigratoryRoute route = new MigratoryRoute();
        Forest forest = new Forest();
        CommandTower tower = new CommandTower();
        harness.setHand(player1, List.of(route));
        harness.setLibrary(player1, List.of(forest, tower));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(route);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, tower);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(tower);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void basicLandcyclingCanDeclineToFindAnAvailableBasicLand() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new MigratoryRoute()));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void basicLandcyclingResolvesWithAnEmptyLibrary() {
        harness.setHand(player1, List.of(new MigratoryRoute()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Migratory Route");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void basicLandcyclingCannotDiscardTheCardWithoutEnoughMana() {
        MigratoryRoute route = new MigratoryRoute();
        harness.setHand(player1, List.of(route));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(route);
        harness.assertNotInGraveyard(player1, "Migratory Route");
        assertThat(gd.stack).isEmpty();
    }
}
