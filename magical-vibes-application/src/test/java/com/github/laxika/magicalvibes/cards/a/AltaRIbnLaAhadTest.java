package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentActionKind;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AltaRIbnLaAhad.class, AssassinInitiate.class, GrizzlyBears.class})
class AltaRIbnLaAhadTest extends BaseCardTest {

    @Test
    void exilesAssassinWithMemoryCounterAndCopiesAllMemoryMarkedCreatures() {
        Card graveyardAssassin = new AssassinInitiate();
        Card exiledAssassin = new AssassinInitiate();
        harness.setGraveyard(player1, List.of(graveyardAssassin));
        harness.setExile(player1, List.of(exiledAssassin));
        gd.exiledCardsWithMemoryCounters.add(exiledAssassin.getId());
        addCreatureReady(player1, new AltaRIbnLaAhad());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        PendingInteraction.MultiGraveyardChoice choice =
                (PendingInteraction.MultiGraveyardChoice) gd.interaction.activeInteraction();
        assertThat(choice.minCount()).isZero();
        harness.handleMultipleCardsChosen(player1, List.of(graveyardAssassin.getId()));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        List<Permanent> tokens = findPermanents(player1, "Assassin Initiate");
        assertThat(tokens).hasSize(2);
        assertThat(tokens).allSatisfy(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.isTapped()).isTrue();
            assertThat(token.isAttackedThisTurn()).isTrue();
            assertThat(token.getAttackTarget()).isEqualTo(player2.getId());
        });
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(graveyardAssassin.getId(), exiledAssassin.getId());
        assertThat(gd.exiledCardsWithMemoryCounters)
                .containsExactlyInAnyOrder(graveyardAssassin.getId(), exiledAssassin.getId());
        assertThat(gd.getDelayedActions(DelayedPermanentAction.class))
                .allMatch(action -> action.kind() == DelayedPermanentActionKind.EXILE_TOKEN_AT_END_OF_COMBAT)
                .hasSize(2);
    }

    @Test
    void doesNotExileNonAssassinCreatureFromGraveyard() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        addCreatureReady(player1, new AltaRIbnLaAhad());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(findPermanents(player1, "Assassin Initiate")).isEmpty();
    }
}
