package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CatharticReunion;
import com.github.laxika.magicalvibes.cards.c.ConsulateSkygate;
import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AetherworksMarvel.class, CatharticReunion.class, ConsulateSkygate.class,
        DukharaPeafowl.class, Forest.class, Mountain.class})
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
                new DukharaPeafowl(), new Forest(), new ConsulateSkygate(), new Mountain(),
                new DukharaPeafowl(), new Forest(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Dukhara Peafowl", "Consulate Skygate", "Dukhara Peafowl");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(marvel.isTapped()).isTrue();

        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dukhara Peafowl");
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

    @Test
    void givesEnergyWhenMarvelItselfIsPutIntoGraveyard() {
        Permanent marvel = addMarvel();
        gd.playerEnergyCounters.put(player1.getId(), 0);

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, marvel));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aetherworks Marvel");
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void givesEnergyForANoncreaturePermanent() {
        addMarvel();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void mayDeclineToCastAndKeepsUnlookedCardsOnTop() {
        addMarvel();
        gd.playerEnergyCounters.put(player1.getId(), 6);
        List<Card> lookedAt = List.of(new DukharaPeafowl(), new Forest(), new ConsulateSkygate(),
                new Mountain(), new Forest(), new Mountain());
        Card unlooked = new Forest();
        java.util.ArrayList<Card> library = new java.util.ArrayList<>(lookedAt);
        library.add(unlooked);
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(unlooked);
        assertThat(gd.playerDecks.get(player1.getId()).subList(1, 7))
                .containsExactlyInAnyOrderElementsOf(lookedAt);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void allLandShortLibraryIsReturnedWithoutOfferingACast() {
        addMarvel();
        gd.playerEnergyCounters.put(player1.getId(), 6);
        List<Card> lands = List.of(new Forest(), new Mountain());
        harness.setLibrary(player1, lands);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrderElementsOf(lands);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
    }

    @Test
    void canCastACreatureDuringAnOpponentsTurnFromAShortLibrary() {
        addMarvel();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);
        gd.playerEnergyCounters.put(player1.getId(), 6);
        harness.setLibrary(player1, List.of(new DukharaPeafowl(), new Forest()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Dukhara Peafowl");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotCastCatharticReunionWithoutTwoCardsToDiscard() {
        addMarvel();
        gd.playerEnergyCounters.put(player1.getId(), 6);
        harness.setHand(player1, List.of());
        Card reunion = new CatharticReunion();
        harness.setLibrary(player1, List.of(reunion, new Forest(), new Mountain()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).contains(reunion).hasSize(3);
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
