package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LoxodonSmiter;
import com.github.laxika.magicalvibes.cards.m.MillennialGargoyle;
import com.github.laxika.magicalvibes.cards.t.TotallyLost;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DinrovaHorror.class, MillennialGargoyle.class, DimirGuildgate.class, TotallyLost.class,
        LoxodonSmiter.class})
class DinrovaHorrorTest extends BaseCardTest {

    @Test
    @DisplayName("ETB bounces the target and its owner discards a card")
    void bouncesTargetAndOwnerDiscards() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new MillennialGargoyle()).getId();
        harness.setHand(player2, new ArrayList<>(List.of(new TotallyLost())));

        cast(targetId);

        harness.assertOnBattlefield(player1, "Dinrova Horror");
        harness.assertNotOnBattlefield(player2, "Millennial Gargoyle");

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());

        // Hand is Totally Lost + the bounced Millennial Gargoyle; discard the returned creature.
        harness.handleCardChosen(player2, indexOf(gd.playerHands.get(player2.getId()), "Millennial Gargoyle"));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .hasSize(1)
                .anyMatch(c -> c.getName().equals("Totally Lost"));
        harness.assertInGraveyard(player2, "Millennial Gargoyle");
    }

    @Test
    @DisplayName("A land is a legal target; its owner then discards")
    void canTargetLand() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new DimirGuildgate()).getId();
        harness.setHand(player2, new ArrayList<>(List.of(new TotallyLost())));

        cast(targetId);

        harness.assertNotOnBattlefield(player2, "Dimir Guildgate");
        harness.assertInHand(player2, "Dimir Guildgate");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
    }

    @Test
    @DisplayName("Targeting your own permanent makes you discard")
    void targetingOwnPermanentDiscardsSelf() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new MillennialGargoyle()).getId();

        cast(targetId);

        harness.assertInHand(player1, "Millennial Gargoyle");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("With an otherwise empty hand, the bounced card is the forced discard")
    void bouncedCardIsForcedDiscard() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new MillennialGargoyle()).getId();
        harness.setHand(player2, new ArrayList<>());

        cast(targetId);

        // The bounced card is the only card in hand, so it is discarded.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.assertInGraveyard(player2, "Millennial Gargoyle");
    }

    @Test
    @DisplayName("An opponent-caused discard puts Loxodon Smiter onto the battlefield")
    void opponentDiscardUsesSmiterReplacement() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new MillennialGargoyle()).getId();
        harness.setHand(player2, List.of(new LoxodonSmiter()));

        cast(targetId);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Loxodon Smiter");
        harness.assertNotInGraveyard(player2, "Loxodon Smiter");
        harness.assertInHand(player2, "Millennial Gargoyle");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's bounced Loxodon Smiter returns to the battlefield when discarded")
    void bouncedSmiterUsesReplacement() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new LoxodonSmiter()).getId();
        harness.setHand(player2, List.of());

        cast(targetId);
        harness.handleCardChosen(player2, 0);

        harness.assertOnBattlefield(player2, "Loxodon Smiter");
        harness.assertNotInGraveyard(player2, "Loxodon Smiter");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Your own ability does not apply Loxodon Smiter's discard replacement")
    void ownBouncedSmiterGoesToGraveyard() {
        UUID targetId = harness.addToBattlefieldAndReturn(player1, new LoxodonSmiter()).getId();

        cast(targetId);
        harness.handleCardChosen(player1, 0);

        harness.assertNotOnBattlefield(player1, "Loxodon Smiter");
        harness.assertInGraveyard(player1, "Loxodon Smiter");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A permanent controlled by another player returns to its owner, who discards")
    void ownerDiscardsRatherThanCurrentController() {
        MillennialGargoyle gargoyle = new MillennialGargoyle();
        gargoyle.setOwnerId(player2.getId());
        UUID targetId = harness.addToBattlefieldAndReturn(player1, gargoyle).getId();
        harness.setHand(player2, List.of(new TotallyLost()));

        cast(targetId);

        harness.assertNotOnBattlefield(player1, "Millennial Gargoyle");
        harness.assertInHand(player2, "Millennial Gargoyle");
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);
        harness.assertInGraveyard(player2, "Totally Lost");
        harness.assertInHand(player2, "Millennial Gargoyle");
    }

    @Test
    @DisplayName("No discard occurs when the target leaves before the ability resolves")
    void missingTargetPreventsDiscard() {
        UUID targetId = harness.addToBattlefieldAndReturn(player2, new MillennialGargoyle()).getId();
        harness.setHand(player2, List.of(new TotallyLost(), new DimirGuildgate()));
        harness.setHand(player1, List.of(new DinrovaHorror()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();

        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Millennial Gargoyle");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName())
                .isEqualTo("Millennial Gargoyle");
        harness.assertInGraveyard(player2, "Totally Lost");
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Dimir Guildgate");
    }

    private void cast(UUID targetId) {
        harness.setHand(player1, new ArrayList<>(List.of(new DinrovaHorror())));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities(); // resolve creature spell -> ETB trigger
        harness.passBothPriorities(); // resolve ETB trigger
    }

    private static int indexOf(List<? extends Card> hand, String name) {
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(name)) {
                return i;
            }
        }
        throw new AssertionError("Card not in hand: " + name);
    }
}
