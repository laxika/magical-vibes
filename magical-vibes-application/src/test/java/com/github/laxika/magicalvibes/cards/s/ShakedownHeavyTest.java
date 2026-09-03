package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShakedownHeavy.class, Forest.class, GrizzlyBears.class})
class ShakedownHeavyTest extends BaseCardTest {

    @Test
    @DisplayName("The defending player may have its controller draw, untap it, and remove it from combat")
    void defendingPlayerAccepts() {
        Permanent heavy = addCreatureReady(player1, new ShakedownHeavy());
        addCreatureReady(player2, new GrizzlyBears());
        setDeck(player1, List.of(new Forest()));
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();
        int defendingHandBefore = gd.playerHands.get(player2.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(heavy.isTapped()).isTrue();
        assertThat(heavy.isAttacking()).isTrue();

        harness.handleMayAbilityChosen(player2, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(defendingHandBefore);
        assertThat(heavy.isTapped()).isFalse();
        assertThat(heavy.isAttacking()).isFalse();
    }

    @Test
    @DisplayName("The defending player may decline")
    void defendingPlayerDeclines() {
        Permanent heavy = addCreatureReady(player1, new ShakedownHeavy());
        addCreatureReady(player2, new GrizzlyBears());
        setDeck(player1, List.of(new Forest()));
        int controllerHandBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(controllerHandBefore);
        assertThat(heavy.isTapped()).isTrue();
        assertThat(heavy.isAttacking()).isTrue();
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
