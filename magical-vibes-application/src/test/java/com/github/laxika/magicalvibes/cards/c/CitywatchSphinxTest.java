package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CitywatchSphinxTest extends BaseCardTest {

    @Test
    @DisplayName("When it dies, surveils two")
    void deathSurveilsTwo() {
        Card topCard = new GrizzlyBears();
        Card secondCard = new Island();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        Permanent sphinx = addReadySphinx(player1);

        sphinx.setMarkedDamage(4);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, secondCard);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(topCard, secondCard);
        assertThat(gd.playerDecks.get(player1.getId()))
                .doesNotContain(topCard, secondCard);
    }

    @Test
    @DisplayName("Another creature's death does not trigger it")
    void anotherCreatureDeathDoesNotTrigger() {
        addReadySphinx(player1);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        bears.setMarkedDamage(2);
        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadySphinx(Player player) {
        Permanent sphinx = new Permanent(new CitywatchSphinx());
        sphinx.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(sphinx);
        return sphinx;
    }
}
