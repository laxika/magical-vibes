package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Molderhulk.class, Forest.class, GrizzlyBears.class, Shock.class})
class MolderhulkTest extends BaseCardTest {

    @Test
    @DisplayName("Costs its full cost with no creature cards in the controller's graveyard")
    void costsFullAmountWithEmptyGraveyard() {
        harness.castFromHand(player1, new Molderhulk(), "{7}{B}{G}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs one less for each creature card in the controller's graveyard")
    void costIsReducedByCreatureCardsInGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.castFromHand(player1, new Molderhulk(), "{5}{B}{G}");

        assertThat(harness.getGameData().stack).hasSize(1);
        assertThat(harness.getGameData().playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Noncreature cards do not reduce its cost")
    void noncreatureCardsDoNotReduceCost() {
        harness.setGraveyard(player1, List.of(new Shock(), new Shock()));
        harness.setHand(player1, List.of(new Molderhulk()));
        addMolderhulkMana(6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Enters by returning a targeted land card from the graveyard")
    void entersAndReturnsTargetedLand() {
        Forest forest = new Forest();
        harness.setGraveyard(player1, List.of(forest));
        harness.castFromHand(player1, new Molderhulk(), "{7}{B}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(forest.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void reductionCannotRemoveColoredManaRequirements() {
        harness.setGraveyard(player1, java.util.stream.IntStream.range(0, 9)
                .mapToObj(i -> (Card) new Molderhulk()).toList());
        harness.setHand(player1, List.of(new Molderhulk()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    void opponentsCreaturesDoNotReduceCost() {
        harness.setGraveyard(player2, List.of(new Molderhulk(), new Molderhulk()));
        harness.setHand(player1, List.of(new Molderhulk()));
        addMolderhulkMana(6);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    void noLandTargetDoesNotPreventCreatureFromEntering() {
        harness.setGraveyard(player1, List.of(new Molderhulk()));
        harness.setGraveyard(player2, List.of(new Forest()));
        harness.castFromHand(player1, new Molderhulk(), "{6}{B}{G}");

        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Molderhulk");
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player2, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void returnsOnlyChosenLandUntapped() {
        Forest chosen = new Forest();
        Forest other = new Forest();
        harness.setGraveyard(player1, List.of(chosen, other, new Molderhulk()));
        harness.castFromHand(player1, new Molderhulk(), "{6}{B}{G}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.stack).hasSize(1);
        harness.assertNotOnBattlefield(player1, "Forest");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(chosen.getId()))
                .singleElement().satisfies(p -> assertThat(p.isTapped()).isFalse());
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(chosen);
    }

    @Test
    void missingTargetDoesNotReturnAnotherLand() {
        Forest chosen = new Forest();
        Forest other = new Forest();
        harness.setGraveyard(player1, List.of(chosen, other));
        harness.castFromHand(player1, new Molderhulk(), "{7}{B}{G}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        gd.playerGraveyards.get(player1.getId()).remove(chosen);
        gd.playerHands.get(player1.getId()).add(chosen);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Forest");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
        assertThat(gd.stack).isEmpty();
    }

    private void addMolderhulkMana(int generic) {
        harness.addMana(player1, ManaColor.COLORLESS, generic);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }
}
