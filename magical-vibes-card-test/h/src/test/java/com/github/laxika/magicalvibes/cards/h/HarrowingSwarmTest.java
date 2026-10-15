package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HarrowingSwarm.class, Forest.class, GrizzlyBears.class, TurnToFrog.class})
class HarrowingSwarmTest extends BaseCardTest {

    @Test
    void manifestsAndReducesTurnFaceUpCostThenPutsCounterOnCreature() {
        Card manifestedCard = new GrizzlyBears();
        Card otherCard = new Forest();
        harness.setHand(player1, List.of(new HarrowingSwarm()));
        harness.setLibrary(player1, List.of(manifestedCard, otherCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.passBothPriorities();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()))
                .findFirst().orElseThrow();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));
        harness.passBothPriorities();

        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void grantsAbilitiesToExistingFaceDownCreaturesEvenWithAnEmptyLibrary() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        existing.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        existing.setManifested(true);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HarrowingSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, 0);
        harness.passBothPriorities();

        assertThat(existing.isFaceDown()).isFalse();
        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotGrantAbilitiesToOpponentsFaceDownCreatures() {
        Permanent opponent = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponent.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        opponent.setManifested(true);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HarrowingSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.turnFaceUp(player2, 0);
        harness.passBothPriorities();

        assertThat(opponent.isFaceDown()).isFalse();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotGrantAbilitiesToCreaturesManifestedAfterResolution() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HarrowingSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent later = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        later.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        later.setManifested(true);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, 0);
        harness.passBothPriorities();

        assertThat(later.isFaceDown()).isFalse();
        assertThat(later.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void repeatedCastsGrantSeparateTriggeredAbilities() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        existing.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        existing.setManifested(true);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HarrowingSwarm(), new HarrowingSwarm()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.passBothPriorities();
        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.passBothPriorities();
        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void losingAbilitiesRemovesGrantedCostReduction() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        existing.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        existing.setManifested(true);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HarrowingSwarm(), new TurnToFrog()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveInstant(player1, 0, existing.getId());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(existing.isFaceDown()).isTrue();
    }

    @Test
    void losingAbilitiesRemovesGrantedTurnFaceUpTrigger() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        existing.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        existing.setManifested(true);
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new HarrowingSwarm(), new TurnToFrog()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.castAndResolveInstant(player1, 0, existing.getId());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, 0);
        harness.passBothPriorities();

        assertThat(existing.isFaceDown()).isFalse();
        assertThat(existing.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canManifestANoncreatureAndPutsTheOtherCardIntoTheGraveyard() {
        Card land = new Forest();
        Card other = new GrizzlyBears();
        harness.setHand(player1, List.of(new HarrowingSwarm()));
        harness.setLibrary(player1, List.of(land, other));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(land.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isFaceDown()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThatThrownBy(() -> harness.turnFaceUp(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void manifestsTheOnlyCardInTheLibraryAndGrantsItTheAbilities() {
        Card onlyCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new HarrowingSwarm()));
        harness.setLibrary(player1, List.of(onlyCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(onlyCard.getId()));
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.turnFaceUp(player1, 0);
        harness.passBothPriorities();

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(manifested.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }
}
