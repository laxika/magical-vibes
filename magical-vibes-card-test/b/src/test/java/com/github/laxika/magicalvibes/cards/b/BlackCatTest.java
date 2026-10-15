package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.t.TragicSlip;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlackCat.class, Shock.class, GrizzlyBears.class, GiantGrowth.class, LightningBolt.class,
        TragicSlip.class})
class BlackCatTest extends BaseCardTest {

    @Test
    @DisplayName("When Black Cat dies, targeted opponent discards a card at random")
    void diesForcesOpponentRandomDiscard() {
        harness.addToBattlefield(player1, new BlackCat());

        // Kill Black Cat with Shock (player2 is active)
        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears(), new GiantGrowth()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID catId = harness.getPermanentId(player1, "Black Cat");
        harness.castAndResolveInstant(player2, 0, catId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        // Only valid target should be the opponent (player2), not player1 (the controller)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities(); // Resolve discard trigger

        // Player2 had 2 non-Shock cards in hand after casting Shock; one is discarded at random.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> !c.getName().equals("Shock"))
                .hasSize(1);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("at random"));
    }

    @Test
    @DisplayName("Death trigger only offers opponents as valid targets")
    void targetFilterExcludesController() {
        harness.addToBattlefield(player1, new BlackCat());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock(), new GrizzlyBears()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID catId = harness.getPermanentId(player1, "Black Cat");
        harness.castAndResolveInstant(player2, 0, catId);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .doesNotContain(player1.getId())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Randomly selects one card from an opponent with multiple cards in hand")
    void discardsOneOfMultipleCards() {
        harness.addToBattlefield(player1, new BlackCat());

        setupPlayer2Active();
        harness.setHand(player2, List.of(
                new Shock(),
                new GrizzlyBears(),
                new GiantGrowth(),
                new LightningBolt()
        ));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID catId = harness.getPermanentId(player1, "Black Cat");
        harness.castAndResolveInstant(player2, 0, catId);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        // Started with 4 cards (Shock was cast), so 3 in hand when trigger resolves,
        // one is discarded at random → 2 left, 1 in graveyard.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> !c.getName().equals("Shock"))
                .hasSize(1);
    }

    @Test
    @DisplayName("Opponent with empty hand results in no discard")
    void emptyHandDoesNothing() {
        harness.addToBattlefield(player1, new BlackCat());

        setupPlayer2Active();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        UUID catId = harness.getPermanentId(player1, "Black Cat");
        harness.castAndResolveInstant(player2, 0, catId);

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        // Only Shock should be in graveyard, no additional random-discarded card
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> !c.getName().equals("Shock"))
                .isEmpty();
    }

    @Test
    @DisplayName("Opponent-controlled Black Cat makes the other player discard only when its trigger resolves")
    void opponentControlledCatTargetsOtherPlayer() {
        UUID catId = harness.addToBattlefieldAndReturn(player2, new BlackCat()).getId();
        BlackCat discarded = new BlackCat();
        TragicSlip retained = new TragicSlip();
        harness.setHand(player1, List.of(new TragicSlip(), discarded));
        harness.setHand(player2, List.of(retained));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, catId);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player1.getId());
        harness.assertInGraveyard(player2, "Black Cat");
        harness.handlePermanentChosen(player2, player1.getId());
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(discarded);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(discarded);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(retained);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Death trigger discards from the opponent's hand at resolution even if it was empty at death")
    void usesHandAtResolution() {
        UUID catId = harness.addToBattlefieldAndReturn(player1, new BlackCat()).getId();
        harness.setHand(player1, List.of(new TragicSlip()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castAndResolveInstant(player1, 0, catId);
        harness.handlePermanentChosen(player1, player2.getId());

        BlackCat arrivingCard = new BlackCat();
        harness.setHand(player2, List.of(arrivingCard));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(arrivingCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void setupPlayer2Active() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
