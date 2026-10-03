package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.event.GameEventEnvelope;
import com.github.laxika.magicalvibes.model.event.GameEventFact;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CelestialReunion.class, AirElemental.class, GrizzlyBears.class})
class CelestialReunionTest extends BaseCardTest {

    @Test
    void searchesAQualifiedCreatureIntoHandWhenBeholdIsNotPaid() {
        Card found = new GrizzlyBears();
        harness.setLibrary(player1, List.of(found));
        harness.setHand(player1, List.of(new CelestialReunion()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 2);
        chooseLibraryCard(0);

        assertThat(gd.playerHands.get(player1.getId())).contains(found);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == found);
    }

    @Test
    void putsFoundCreatureOntoBattlefieldWhenItMatchesBeheldType() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Card found = new AirElemental();
        harness.setLibrary(player1, List.of(found));
        harness.setHand(player1, List.of(new CelestialReunion()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorceryWithBehold(player1, 0, 5, CardSubtype.ELEMENTAL,
                List.of(first.getId(), second.getId()), List.of());
        harness.passBothPriorities();
        chooseLibraryCard(0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == found);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(found);
    }

    @Test
    void keepsFoundCreatureInHandWhenItDoesNotMatchBeheldType() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Card found = new GrizzlyBears();
        harness.setLibrary(player1, List.of(found));
        harness.setHand(player1, List.of(new CelestialReunion()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorceryWithBehold(player1, 0, 2, CardSubtype.ELEMENTAL,
                List.of(first.getId(), second.getId()), List.of());
        harness.passBothPriorities();
        chooseLibraryCard(0);

        assertThat(gd.playerHands.get(player1.getId())).contains(found);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == found);
    }

    @Test
    void revealsBothBeheldHandCardsToBothPlayersWhileLeavingThemInHand() throws Exception {
        Card first = new AirElemental();
        Card second = new AirElemental();
        Card found = new AirElemental();
        harness.setHand(player1, List.of(first, new CelestialReunion(), second));
        harness.setLibrary(player1, List.of(found));
        harness.addMana(player1, ManaColor.GREEN, 6);
        List<GameEventEnvelope> events = new ArrayList<>();

        try (AutoCloseable ignored = harness.subscribeToGameEvents(batch ->
                batch.events().forEach(events::add))) {
            harness.castSorceryWithBehold(player1, 1, 5, CardSubtype.ELEMENTAL,
                    List.of(), List.of(0, 2));
        }

        assertThat(events)
                .filteredOn(event -> event.fact() instanceof GameEventFact.PrivateReveal
                        && event.audience().isVisibleTo(player1.getId())
                        && event.audience().isVisibleTo(player2.getId())
                        && ((GameEventFact.PrivateReveal) event.fact()).zone() == GameEventFact.RevealZone.HAND
                        && ((GameEventFact.PrivateReveal) event.fact()).subjectPlayerId().equals(player1.getId()))
                .flatExtracting(event -> ((GameEventFact.PrivateReveal) event.fact()).cards())
                .extracting(GameEventFact.CardSnapshot::cardId)
                .contains(first.getId(), second.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);

        harness.passBothPriorities();
        chooseLibraryCard(0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == found);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void canBeholdOneTappedCreatureAndOneCardInHand() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        first.setTapped(true);
        Card second = new AirElemental();
        Card found = new AirElemental();
        harness.setHand(player1, List.of(new CelestialReunion(), second));
        harness.setLibrary(player1, List.of(found));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.castSorceryWithBehold(player1, 0, 5, CardSubtype.ELEMENTAL,
                List.of(first.getId()), List.of(1));
        harness.passBothPriorities();
        chooseLibraryCard(0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first);
        assertThat(first.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(found);
                    assertThat(permanent.isTapped()).isFalse();
                });
    }

    @Test
    void searchesForCreatureWithManaValueLessThanX() {
        Card found = new GrizzlyBears();
        Card tooExpensive = new AirElemental();
        Card noncreature = new CelestialReunion();
        harness.setLibrary(player1, List.of(tooExpensive, noncreature, found));
        harness.setHand(player1, List.of(new CelestialReunion()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castAndResolveSorcery(player1, 0, 3);
        chooseLibraryCard(0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(found);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(tooExpensive, noncreature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void zeroXDoesNotFindPositiveManaValueCreature() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player1, List.of(new CelestialReunion()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void mayFailToFindEvenWithAnEligibleCreature() {
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));
        harness.setHand(player1, List.of(new CelestialReunion()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 2);
        chooseLibraryCard(-1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(creature);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void resolvesWithAnEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CelestialReunion()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castAndResolveSorcery(player1, 0, 2);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Celestial Reunion");
    }

    @Test
    void cannotBeholdTheSameCreatureTwice() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        harness.setHand(player1, List.of(new CelestialReunion()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castSorceryWithBehold(player1, 0, 5,
                CardSubtype.ELEMENTAL, List.of(creature.getId(), creature.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotBeholdAnOpponentsCreature() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        harness.setHand(player1, List.of(new CelestialReunion()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castSorceryWithBehold(player1, 0, 5,
                CardSubtype.ELEMENTAL, List.of(own.getId(), opposing.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotBeholdCreaturesThatDoNotShareTheChosenType() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        Permanent bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new CelestialReunion()));
        harness.addMana(player1, ManaColor.GREEN, 6);

        assertThatThrownBy(() -> harness.castSorceryWithBehold(player1, 0, 5,
                CardSubtype.ELEMENTAL, List.of(elemental.getId(), bear.getId()), List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private void chooseLibraryCard(int index) {
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNotNull();
        harness.handleCardChosen(player1, index);
    }
}
