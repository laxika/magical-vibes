package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JunjiTheMidnightSky.class, Forest.class, GrizzlyBears.class})
class JunjiTheMidnightSkyTest extends BaseCardTest {

    private static final String DISCARD_MODE = "Each opponent discards two cards and loses 2 life";
    private static final String REANIMATE_MODE =
            "Put target non-Dragon creature card from a graveyard onto the battlefield under your control. You lose 2 life";

    @Test
    @DisplayName("The discard mode makes each opponent discard two cards and lose two life")
    void discardModeDiscardsAndLosesLife() {
        harness.setHand(player2, List.of(new Forest(), new GrizzlyBears()));
        harness.addToBattlefield(player1, new JunjiTheMidnightSky());

        killJunji();
        harness.handleListChoice(player1, DISCARD_MODE);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The reanimation mode targets a non-Dragon creature card from any graveyard")
    void reanimationModeReturnsNonDragonCreatureAndLosesLife() {
        Card invalidLand = new Forest();
        Card invalidDragon = new JunjiTheMidnightSky();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(invalidLand, invalidDragon, creature));
        harness.addToBattlefield(player1, new JunjiTheMidnightSky());

        killJunji();
        harness.handleListChoice(player1, REANIMATE_MODE);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(Permanent::getCard)
                .extracting(Card::getId)
                .contains(creature.getId());
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(invalidLand.getId(), invalidDragon.getId());
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("The discard mode still loses life when the opponent has no cards")
    void discardModeWithEmptyHandStillLosesLife() {
        harness.setHand(player2, List.of());
        harness.addToBattlefield(player1, new JunjiTheMidnightSky());

        killJunji();
        harness.handleListChoice(player1, DISCARD_MODE);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The discard mode discards the only card in a short hand and still loses life")
    void discardModeWithOneCardDiscardsItAndLosesLife() {
        harness.setHand(player2, List.of(new Forest()));
        harness.addToBattlefield(player1, new JunjiTheMidnightSky());

        killJunji();
        harness.handleListChoice(player1, DISCARD_MODE);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The reanimation mode can return a creature from its controller's graveyard")
    void reanimationModeReturnsOwnCreature() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addToBattlefield(player1, new JunjiTheMidnightSky());

        killJunji();
        harness.handleListChoice(player1, REANIMATE_MODE);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Junji, the Midnight Sky");
        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("The reanimation mode does not lose life when its sole target leaves the graveyard")
    void reanimationModeWithRemovedTargetDoesNotLoseLife() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(creature));
        harness.addToBattlefield(player1, new JunjiTheMidnightSky());

        killJunji();
        harness.handleListChoice(player1, REANIMATE_MODE);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player2, List.of());
        harness.setExile(player2, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    private void killJunji() {
        Permanent junji = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof JunjiTheMidnightSky)
                .findFirst()
                .orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, junji));
        harness.passBothPriorities();
    }
}
