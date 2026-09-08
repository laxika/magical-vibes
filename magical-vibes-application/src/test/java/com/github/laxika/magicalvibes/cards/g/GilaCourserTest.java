package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GilaCourser.class, GrizzlyBears.class})
class GilaCourserTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while saddled exiles the top card with play permission")
    void attacksWhileSaddled() {
        Card topCard = new GrizzlyBears();
        Permanent courser = addCreatureReady(player1, new GilaCourser());
        courser.setSaddled(true);
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Attacking while unsaddled does not exile the top card")
    void doesNotTriggerWhileUnsaddled() {
        Card topCard = new GrizzlyBears();
        addCreatureReady(player1, new GilaCourser());
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    @Test
    @DisplayName("The attack trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Card topCard = new GrizzlyBears();
        Permanent courser = addCreatureReady(player1, new GilaCourser());
        harness.setLibrary(player1, List.of(topCard));

        declareAttackers(player1, List.of(0));
        courser.setSaddled(true);
        resolveAllTriggers();

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }
}
