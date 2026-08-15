package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AetherworksMarvelTest extends BaseCardTest {

    @Test
    void givesEnergyWhenYourTokenIsPutIntoGraveyard() {
        addMarvel();
        Permanent token = harness.addToBattlefieldAndReturn(player1, tokenCreature());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, token));
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void doesNotGiveEnergyForAnOpponentsPermanent() {
        addMarvel();
        gd.playerEnergyCounters.put(player1.getId(), 0);
        Permanent token = harness.addToBattlefieldAndReturn(player2, tokenCreature());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, token));
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void looksAtSixCardsAndOffersAnyNonlandSpellForFree() {
        Permanent marvel = addMarvel();
        gd.playerEnergyCounters.put(player1.getId(), 6);
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new Forest(), new LlanowarElves(), new Mountain(),
                new GrizzlyBears(), new Forest(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Grizzly Bears", "Llanowar Elves", "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(marvel.isTapped()).isTrue();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
    }

    @Test
    void cannotActivateWithoutSixEnergy() {
        addMarvel();
        gd.playerEnergyCounters.put(player1.getId(), 5);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("six energy counters");
    }

    private Permanent addMarvel() {
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.forceActivePlayer(player1);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player1, new AetherworksMarvel());
    }

    private static Card tokenCreature() {
        Card card = new Card();
        card.setName("Test Token");
        card.setType(CardType.CREATURE);
        card.setPower(1);
        card.setToughness(1);
        card.setToken(true);
        return card;
    }
}
