package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IrreverentGremlin.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class IrreverentGremlinTest extends BaseCardTest {

    @Test
    void mayDiscardAndDrawWhenSmallAllyEnters() {
        Card discarded = new Forest();
        Card drawn = new HillGiant();
        harness.addToBattlefield(player1, new IrreverentGremlin());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), discarded)));
        setDeck(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void decliningMayDoesNotDiscardOrDraw() {
        Card discarded = new Forest();
        Card drawn = new HillGiant();
        harness.addToBattlefield(player1, new IrreverentGremlin());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), discarded)));
        setDeck(player1, List.of(drawn));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(drawn);
    }

    @Test
    void doesNotTriggerForLargeAlly() {
        harness.addToBattlefield(player1, new IrreverentGremlin());
        harness.setHand(player1, new ArrayList<>(List.of(new HillGiant())));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotTriggerForItsOwnEntry() {
        Card discarded = new Forest();
        harness.setHand(player1, new ArrayList<>(List.of(new IrreverentGremlin(), discarded)));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);
    }

    @Test
    void triggersOnlyOnceEachTurn() {
        Card discarded = new Forest();
        Card drawn = new HillGiant();
        harness.addToBattlefield(player1, new IrreverentGremlin());
        harness.setHand(player1, new ArrayList<>(List.of(new GrizzlyBears(), discarded, new GrizzlyBears())));
        setDeck(player1, List.of(drawn, new HillGiant()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(discarded);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
