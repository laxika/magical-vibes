package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.p.PiousInterdiction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadeyeTracker.class, Forest.class, RaptorCompanion.class, PiousInterdiction.class})
class DeadeyeTrackerTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles two target cards from opponent's graveyard")
    void exilesTwoCardsFromOpponentGraveyard() {
        Permanent tracker = addReadyTracker(player1);
        Card card1 = new RaptorCompanion();
        Card card2 = new PiousInterdiction();
        harness.setGraveyard(player2, List.of(card1, card2));
        harness.addMana(player1, ManaColor.BLACK, 2);
        // Put a land on top so explore puts it into hand (no may-ability prompt)
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);
        harness.activateAbilityWithGraveyardTargets(player1, trackerIndex, 0,
                List.of(card1.getId(), card2.getId()));
        harness.passBothPriorities();

        // Both cards exiled from opponent's graveyard
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Raptor Companion"))
                .anyMatch(c -> c.getName().equals("Pious Interdiction"));
    }

    @Test
    @DisplayName("Deadeye Tracker explores after exiling — land goes to hand")
    void exploresAfterExiling_landGoesToHand() {
        Permanent tracker = addReadyTracker(player1);
        Card card1 = new RaptorCompanion();
        Card card2 = new PiousInterdiction();
        harness.setGraveyard(player2, List.of(card1, card2));
        harness.addMana(player1, ManaColor.BLACK, 2);

        Card land = new Forest();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);
        harness.activateAbilityWithGraveyardTargets(player1, trackerIndex, 0,
                List.of(card1.getId(), card2.getId()));
        harness.passBothPriorities();

        // Land from explore should be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("Deadeye Tracker explores after exiling — non-land adds +1/+1 counter")
    void exploresAfterExiling_nonLandAddsCounter() {
        Permanent tracker = addReadyTracker(player1);
        Card card1 = new RaptorCompanion();
        Card card2 = new PiousInterdiction();
        harness.setGraveyard(player2, List.of(card1, card2));
        harness.addMana(player1, ManaColor.BLACK, 2);

        // Non-land on top
        gd.playerDecks.get(player1.getId()).addFirst(new RaptorCompanion());

        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);
        harness.activateAbilityWithGraveyardTargets(player1, trackerIndex, 0,
                List.of(card1.getId(), card2.getId()));
        harness.passBothPriorities();

        // +1/+1 counter from exploring
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        // May-ability prompt for putting the card into graveyard
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Cannot target cards in controller's own graveyard")
    void cannotTargetOwnGraveyard() {
        Permanent tracker = addReadyTracker(player1);
        Card card1 = new RaptorCompanion();
        Card card2 = new PiousInterdiction();
        harness.setGraveyard(player1, List.of(card1, card2));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, trackerIndex, 0,
                List.of(card1.getId(), card2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent's graveyard");
    }

    @Test
    @DisplayName("Must select exactly two target cards")
    void mustSelectExactlyTwoTargets() {
        Permanent tracker = addReadyTracker(player1);
        Card card1 = new RaptorCompanion();
        harness.setGraveyard(player2, List.of(card1));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, trackerIndex, 0,
                List.of(card1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly 2");
    }

    @Test
    @DisplayName("Activating ability taps Deadeye Tracker")
    void activatingTapsTracker() {
        Permanent tracker = addReadyTracker(player1);
        Card card1 = new RaptorCompanion();
        Card card2 = new PiousInterdiction();
        harness.setGraveyard(player2, List.of(card1, card2));
        harness.addMana(player1, ManaColor.BLACK, 2);
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        assertThat(tracker.isTapped()).isFalse();

        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);
        harness.activateAbilityWithGraveyardTargets(player1, trackerIndex, 0,
                List.of(card1.getId(), card2.getId()));

        assertThat(tracker.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutEnoughMana() {
        Permanent tracker = addReadyTracker(player1);
        Card card1 = new RaptorCompanion();
        Card card2 = new PiousInterdiction();
        harness.setGraveyard(player2, List.of(card1, card2));
        harness.addMana(player1, ManaColor.BLACK, 1); // only 1 black, need {1}{B}

        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, trackerIndex, 0,
                List.of(card1.getId(), card2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate when already tapped")
    void cannotActivateWhenTapped() {
        Permanent tracker = addReadyTracker(player1);
        tracker.tap();
        Card card1 = new RaptorCompanion();
        Card card2 = new PiousInterdiction();
        harness.setGraveyard(player2, List.of(card1, card2));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, trackerIndex, 0,
                List.of(card1.getId(), card2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        Permanent tracker = harness.addToBattlefieldAndReturn(player1, new DeadeyeTracker());
        tracker.setSummoningSick(true);

        Card card1 = new RaptorCompanion();
        Card card2 = new PiousInterdiction();
        harness.setGraveyard(player2, List.of(card1, card2));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, trackerIndex, 0,
                List.of(card1.getId(), card2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Fizzles if targets removed from graveyard before resolution")
    void fizzlesIfTargetsRemoved() {
        Permanent tracker = addReadyTracker(player1);
        Card card1 = new RaptorCompanion();
        Card card2 = new PiousInterdiction();
        harness.setGraveyard(player2, List.of(card1, card2));
        harness.addMana(player1, ManaColor.BLACK, 2);
        gd.playerDecks.get(player1.getId()).addFirst(new Forest());

        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);
        harness.activateAbilityWithGraveyardTargets(player1, trackerIndex, 0,
                List.of(card1.getId(), card2.getId()));

        // Remove targets before resolution
        gd.playerGraveyards.get(player2.getId()).clear();

        harness.passBothPriorities();

        // Neither card should be in exile (they were removed before resolution)
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void exploresWithEmptyLibraryStillAddsCounter() {
        Permanent tracker = addReadyTracker(player1);
        Card first = new RaptorCompanion();
        Card second = new PiousInterdiction();
        harness.setGraveyard(player2, List.of(first, second));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void cannotTargetSameGraveyardCardTwice() {
        Permanent tracker = addReadyTracker(player1);
        Card target = new RaptorCompanion();
        harness.setGraveyard(player2, List.of(target));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(target.getId(), target.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tracker.isTapped()).isFalse();
    }

    @Test
    void exploresWhenOneTargetBecomesIllegal() {
        addReadyTracker(player1);
        Card first = new RaptorCompanion();
        Card second = new PiousInterdiction();
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(first, second));
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(first.getId(), second.getId()));
        gd.playerGraveyards.get(player2.getId()).remove(first);
        gd.addCardToHand(player2.getId(), first);
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(second);
        assertThat(gd.playerHands.get(player1.getId())).contains(land);
    }

    @Test
    void doesNotExploreWhenBothTargetsBecomeIllegal() {
        Permanent tracker = addReadyTracker(player1);
        Card first = new RaptorCompanion();
        Card second = new PiousInterdiction();
        Card land = new Forest();
        harness.setGraveyard(player2, List.of(first, second));
        harness.setLibrary(player1, List.of(land));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(first.getId(), second.getId()));
        harness.setGraveyard(player2, List.of());
        gd.addCardToHand(player2.getId(), first);
        gd.addCardToHand(player2.getId(), second);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(land);
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void canPutExploredNonlandIntoGraveyard() {
        Permanent tracker = addReadyTracker(player1);
        Card first = new RaptorCompanion();
        Card second = new PiousInterdiction();
        Card revealed = new RaptorCompanion();
        harness.setGraveyard(player2, List.of(first, second));
        harness.setLibrary(player1, List.of(revealed));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(revealed);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void canLeaveExploredNonlandOnTop() {
        Permanent tracker = addReadyTracker(player1);
        Card first = new RaptorCompanion();
        Card second = new PiousInterdiction();
        Card revealed = new RaptorCompanion();
        harness.setGraveyard(player2, List.of(first, second));
        harness.setLibrary(player1, List.of(revealed));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0,
                List.of(first.getId(), second.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(revealed);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(revealed);
    }

    private Permanent addReadyTracker(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DeadeyeTracker());
        perm.setSummoningSick(false);
        return perm;
    }
}
