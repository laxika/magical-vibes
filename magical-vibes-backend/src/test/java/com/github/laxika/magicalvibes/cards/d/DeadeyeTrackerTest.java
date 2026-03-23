package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardsFromOpponentGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.ExploreEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DeadeyeTrackerTest extends BaseCardTest {

    // ===== Card structure =====

    @Test
    @DisplayName("Has activated ability with ExileTargetCardsFromOpponentGraveyardEffect and ExploreEffect")
    void hasActivatedAbility() {
        DeadeyeTracker card = new DeadeyeTracker();

        assertThat(card.getActivatedAbilities()).hasSize(1);

        var ability = card.getActivatedAbilities().getFirst();
        assertThat(ability.isRequiresTap()).isTrue();
        assertThat(ability.getManaCost()).isEqualTo("{1}{B}");
        assertThat(ability.getEffects())
                .anyMatch(e -> e instanceof ExileTargetCardsFromOpponentGraveyardEffect ex && ex.count() == 2)
                .anyMatch(e -> e instanceof ExploreEffect);
    }

    // ===== Activated ability: exile two target cards from opponent's graveyard =====

    @Test
    @DisplayName("Exiles two target cards from opponent's graveyard")
    void exilesTwoCardsFromOpponentGraveyard() {
        Permanent tracker = addReadyTracker(player1);
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2)));
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
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("Deadeye Tracker explores after exiling — land goes to hand")
    void exploresAfterExiling_landGoesToHand() {
        Permanent tracker = addReadyTracker(player1);
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2)));
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
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        // Non-land on top
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());

        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);
        harness.activateAbilityWithGraveyardTargets(player1, trackerIndex, 0,
                List.of(card1.getId(), card2.getId()));
        harness.passBothPriorities();

        // +1/+1 counter from exploring
        assertThat(tracker.getPlusOnePlusOneCounters()).isEqualTo(1);
        // May-ability prompt for putting the card into graveyard
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MAY_ABILITY_CHOICE);
    }

    // ===== Targeting restrictions =====

    @Test
    @DisplayName("Cannot target cards in controller's own graveyard")
    void cannotTargetOwnGraveyard() {
        Permanent tracker = addReadyTracker(player1);
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player1, new ArrayList<>(List.of(card1, card2)));
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
        Card card1 = new GrizzlyBears();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, trackerIndex, 0,
                List.of(card1.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("exactly 2");
    }

    // ===== Cost validation =====

    @Test
    @DisplayName("Activating ability taps Deadeye Tracker")
    void activatingTapsTracker() {
        Permanent tracker = addReadyTracker(player1);
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2)));
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
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2)));
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
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, trackerIndex, 0,
                List.of(card1.getId(), card2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        DeadeyeTracker card = new DeadeyeTracker();
        Permanent tracker = new Permanent(card);
        tracker.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(tracker);

        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2)));
        harness.addMana(player1, ManaColor.BLACK, 2);

        int trackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(tracker);

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(player1, trackerIndex, 0,
                List.of(card1.getId(), card2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Fizzles if targets removed from graveyard before resolution")
    void fizzlesIfTargetsRemoved() {
        Permanent tracker = addReadyTracker(player1);
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player2, new ArrayList<>(List.of(card1, card2)));
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

    // ===== Helpers =====

    private Permanent addReadyTracker(Player player) {
        DeadeyeTracker card = new DeadeyeTracker();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
