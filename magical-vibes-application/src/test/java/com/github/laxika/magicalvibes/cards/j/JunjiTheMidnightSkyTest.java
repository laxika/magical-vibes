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

    private void killJunji() {
        Permanent junji = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof JunjiTheMidnightSky)
                .findFirst()
                .orElseThrow();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, junji));
        harness.passBothPriorities();
    }
}
