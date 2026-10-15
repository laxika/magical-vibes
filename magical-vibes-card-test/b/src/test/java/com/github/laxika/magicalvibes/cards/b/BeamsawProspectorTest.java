package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BeamsawProspector.class, Shock.class, Forest.class})
class BeamsawProspectorTest extends BaseCardTest {

    @Test
    @DisplayName("When Beamsaw Prospector dies, its controller creates a Lander token")
    void deathTriggerCreatesLanderToken() {
        Permanent prospector = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, prospector.getId());

        harness.assertInGraveyard(player1, "Beamsaw Prospector");
        assertThat(findPermanents(player1, "Lander")).isEmpty();

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Lander")).hasSize(1);
        assertThat(findPermanents(player2, "Lander")).isEmpty();
    }

    @Test
    @DisplayName("The Lander created by Beamsaw Prospector searches for a tapped basic land")
    void createdLanderSearchesForTappedBasicLand() {
        harness.addToBattlefield(player1, new BeamsawProspector());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0,
                harness.getPermanentId(player1, "Beamsaw Prospector"));
        harness.passBothPriorities();

        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        Permanent lander = findPermanents(player1, "Lander").getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1,
                harness.getGameData().playerBattlefields.get(player1.getId()).indexOf(lander),
                0, null, null);
        harness.passBothPriorities();

        assertThat(harness.getGameData().interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.LibrarySearch.class);
    }

    @Test
    @DisplayName("Lander is sacrificed as a cost and puts only a basic land onto the battlefield tapped")
    void landerSacrificeAndSearchResolveCompletely() {
        Permanent lander = createLander();
        Card forest = new Forest();
        Card creature = new BeamsawProspector();
        harness.setLibrary(player1, List.of(creature, forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lander),
                0, null, null);

        harness.assertNotOnBattlefield(player1, "Lander");
        assertThat(lander.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(forest);
        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        harness.assertNotOnBattlefield(player2, "Forest");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Lander can fail to find even when a basic land is available")
    void landerCanFailToFind() {
        Permanent lander = createLander();
        Card forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lander),
                0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertNotOnBattlefield(player1, "Lander");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Lander resolves with an empty library without requesting a choice")
    void landerSearchesEmptyLibrary() {
        Permanent lander = createLander();
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(lander),
                0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lander");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private Permanent createLander() {
        Permanent prospector = harness.addToBattlefieldAndReturn(player1, new BeamsawProspector());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, prospector.getId());
        harness.passBothPriorities();
        return findPermanent(player1, "Lander");
    }
}
