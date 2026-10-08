package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulShackledZombie.class, Forest.class, GrizzlyBears.class})
class SoulShackledZombieTest extends BaseCardTest {

    @Test
    @DisplayName("Exiling a creature card makes each opponent lose 2 life and gains 2 life")
    void creatureCardExiledAppliesLifeRider() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(creature, land));
        castAndResolveCreatureToTargetingPrompt();

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature, land);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Exiling no creature card does not apply the life rider")
    void noCreatureCardExiledDoesNotApplyLifeRider() {
        Card firstLand = new Forest();
        Card secondLand = new Forest();
        harness.setGraveyard(player2, List.of(firstLand, secondLand));
        castAndResolveCreatureToTargetingPrompt();

        harness.handleMultipleCardsChosen(player1, List.of(firstLand.getId(), secondLand.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(firstLand, secondLand);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The two chosen cards must come from a single graveyard")
    void chosenCardsMustShareGraveyard() {
        Card ownCard = new Forest();
        Card opponentCard = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        castAndResolveCreatureToTargetingPrompt();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(
                player1, List.of(ownCard.getId(), opponentCard.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("single graveyard");

        harness.handleMultipleCardsChosen(player1, List.of(opponentCard.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentCard);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    void choosingZeroCardsDoesNotChangeLifeOrGraveyard() {
        Card creature = new SoulShackledZombie();
        harness.setGraveyard(player2, List.of(creature));
        castAndResolveCreatureToTargetingPrompt();

        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void twoCreaturesFromOwnGraveyardApplyLifeRiderOnlyOnce() {
        Card firstCreature = new SoulShackledZombie();
        Card secondCreature = new SoulShackledZombie();
        harness.setGraveyard(player1, List.of(firstCreature, secondCreature));
        castAndResolveCreatureToTargetingPrompt();

        harness.handleMultipleCardsChosen(player1, List.of(firstCreature.getId(), secondCreature.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(firstCreature, secondCreature);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void creatureRemovedBeforeResolutionDoesNotApplyLifeRiderForRemainingLand() {
        Card creature = new SoulShackledZombie();
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(creature, land));
        castAndResolveCreatureToTargetingPrompt();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));

        harness.setGraveyard(player2, List.of(land));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(land);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void landRemovedBeforeResolutionStillAllowsCreatureLifeRider() {
        Card creature = new SoulShackledZombie();
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(creature, land));
        castAndResolveCreatureToTargetingPrompt();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId(), land.getId()));

        harness.setGraveyard(player2, List.of(creature));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    void emptyGraveyardsDoNotPreventCreatureEnteringOrChangeLife() {
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of());
        harness.castFromHand(player1, new SoulShackledZombie(), "{3}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Soul-Shackled Zombie");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void allTargetsRemovedBeforeResolutionDoNotApplyLifeRider() {
        Card creature = new SoulShackledZombie();
        harness.setGraveyard(player2, List.of(creature));
        castAndResolveCreatureToTargetingPrompt();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void castAndResolveCreatureToTargetingPrompt() {
        harness.castFromHand(player1, new SoulShackledZombie(), "{3}{B}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
    }
}
