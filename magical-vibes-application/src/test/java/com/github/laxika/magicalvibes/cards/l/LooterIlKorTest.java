package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.ViridianLongbow;
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

@CardUsed({LooterIlKor.class, GrizzlyBears.class, ViridianLongbow.class})
class LooterIlKorTest extends BaseCardTest {

    @Test
    @DisplayName("Draws and then discards when it deals combat damage to an opponent")
    void drawsThenDiscardsAfterCombatDamage() {
        Permanent looter = addCreatureReady(player1, new LooterIlKor());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new GrizzlyBears()));

        looter.setAttacking(true);
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Triggers on noncombat damage to an opponent")
    void triggersOnNoncombatDamage() {
        Permanent looter = addCreatureReady(player1, new LooterIlKor());
        Permanent longbow = harness.addToBattlefieldAndReturn(player1, new ViridianLongbow());
        longbow.setAttachedTo(looter.getId());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        setDeck(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    private void setDeck(Player player, List<Card> cards) {
        gd.playerDecks.get(player.getId()).clear();
        gd.playerDecks.get(player.getId()).addAll(cards);
    }
}
