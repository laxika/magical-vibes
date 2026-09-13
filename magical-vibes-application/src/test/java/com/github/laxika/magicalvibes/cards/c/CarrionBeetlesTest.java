package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CarrionBeetles.class, Forest.class})
class CarrionBeetlesTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles up to three target cards from a single graveyard")
    void exilesThreeCardsFromSingleGraveyard() {
        Permanent beetles = addReadyBeetles();
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card untouched = new Forest();
        harness.setGraveyard(player2, List.of(first, second, third, untouched));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbilityWithGraveyardTargets(player1, index(beetles), 0,
                List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .containsExactlyInAnyOrder(first, second, third);
        assertThat(beetles.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Up to three allows exiling fewer cards")
    void exilesFewerThanThreeCards() {
        Permanent beetles = addReadyBeetles();
        Card target = new Forest();
        Card untouched = new Forest();
        harness.setGraveyard(player1, List.of(target, untouched));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbilityWithGraveyardTargets(player1, index(beetles), 0, List.of(target.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(untouched);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(target);
    }

    @Test
    @DisplayName("Cannot select cards from multiple graveyards")
    void rejectsTargetsFromMultipleGraveyards() {
        Permanent beetles = addReadyBeetles();
        Card ownCard = new Forest();
        Card opposingCard = new Forest();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opposingCard));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(beetles), 0, List.of(ownCard.getId(), opposingCard.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ownCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCard);
        assertThat(beetles.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Can choose zero target cards")
    void canChooseZeroCards() {
        Permanent beetles = addReadyBeetles();
        Card untouched = new Forest();
        harness.setGraveyard(player2, List.of(untouched));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.activateAbilityWithGraveyardTargets(player1, index(beetles), 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(untouched);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(beetles.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target more than three cards")
    void rejectsMoreThanThreeCards() {
        Permanent beetles = addReadyBeetles();
        Card first = new Forest();
        Card second = new Forest();
        Card third = new Forest();
        Card fourth = new Forest();
        harness.setGraveyard(player2, List.of(first, second, third, fourth));
        harness.addMana(player1, ManaColor.BLACK, 3);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(beetles), 0,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more than 3");

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .containsExactly(first, second, third, fourth);
        assertThat(beetles.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without the full mana cost")
    void cannotActivateWithoutEnoughMana() {
        Permanent beetles = addReadyBeetles();
        Card target = new Forest();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, index(beetles), 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(beetles.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Advertises cards in graveyards as valid targets")
    void advertisesGraveyardCardsAsValidTargets() {
        Permanent beetles = addReadyBeetles();
        Card target = new Forest();
        harness.setGraveyard(player2, List.of(target));

        var response = harness.getValidTargetService().computeValidTargetsForAbility(
                gd,
                beetles.getCard(),
                beetles.getCard().getActivatedAbilities().getFirst(),
                player1.getId(),
                index(beetles));

        assertThat(response.validGraveyardCardIds()).containsExactly(target.getId());
        assertThat(response.minTargets()).isZero();
        assertThat(response.maxTargets()).isEqualTo(3);
    }

    private Permanent addReadyBeetles() {
        return addCreatureReady(player1, new CarrionBeetles());
    }

    private int index(Permanent beetles) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(beetles);
    }
}
