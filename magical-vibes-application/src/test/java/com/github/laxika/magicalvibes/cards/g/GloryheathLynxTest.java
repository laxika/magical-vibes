package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GloryheathLynx.class, Plains.class, Forest.class})
class GloryheathLynxTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking while saddled searches for a basic Plains")
    void attacksWhileSaddledSearchesForBasicPlains() {
        Permanent lynx = addCreatureReady(player1, new GloryheathLynx());
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));
        lynx.setSaddled(true);

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(card -> card.getName()).containsExactly("Plains");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("Attacking while not saddled does not search")
    void doesNotSearchWhenNotSaddled() {
        addCreatureReady(player1, new GloryheathLynx());
        harness.setLibrary(player1, List.of(new Plains()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Plains"));
    }

    @Test
    @DisplayName("The trigger checks saddled when attackers are declared")
    void checksSaddledAtDeclaration() {
        Permanent lynx = addCreatureReady(player1, new GloryheathLynx());
        harness.setLibrary(player1, List.of(new Plains()));

        declareAttackers(player1, List.of(0));
        lynx.setSaddled(true);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("Saddle 2 taps another creature and enables the Plains search")
    void saddleEnablesAttackTrigger() {
        Permanent lynx = addCreatureReady(player1, new GloryheathLynx());
        Permanent saddler = addCreatureReady(player1, new GloryheathLynx());
        saddler.setSummoningSick(true);
        harness.setLibrary(player1, List.of(new Plains()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(saddler.isTapped()).isTrue();
        assertThat(lynx.isTapped()).isFalse();
        assertThat(lynx.isSaddled()).isTrue();
        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("The search resolves even after the saddled attacker leaves")
    void searchResolvesAfterSourceLeaves() {
        Permanent lynx = addCreatureReady(player1, new GloryheathLynx());
        lynx.setSaddled(true);
        harness.setLibrary(player1, List.of(new Plains()));

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player1, List.of(0)));
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(lynx);
        gd.playerGraveyards.get(player1.getId()).add(lynx.getCard());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Plains");
    }

    @Test
    @DisplayName("A restricted search may fail to find an available Plains")
    void mayFailToFind() {
        Permanent lynx = addCreatureReady(player1, new GloryheathLynx());
        lynx.setSaddled(true);
        harness.setLibrary(player1, List.of(new Plains(), new Forest()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }

    @Test
    @DisplayName("A saddled attack with no basic Plains completes without finding a card")
    void noMatchingPlains() {
        Permanent lynx = addCreatureReady(player1, new GloryheathLynx());
        lynx.setSaddled(true);
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Combat damage gains life without requiring saddle")
    void lifelinkWithoutSaddle() {
        addCreatureReady(player1, new GloryheathLynx());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveCombat();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }
}
