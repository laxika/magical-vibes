package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.t.TorporOrb;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Jailbreak.class, AirElemental.class, GrizzlyBears.class, LlanowarElves.class, TorporOrb.class})
class JailbreakTest extends BaseCardTest {

    @Test
    void returnsOpponentPermanentAndThenOwnPermanentWithEqualOrLesserManaValue() {
        Card opponentPermanent = new GrizzlyBears();
        Card lowerManaValue = new LlanowarElves();
        Card equalManaValue = new GrizzlyBears();
        Card higherManaValue = new AirElemental();
        prepare(opponentPermanent, lowerManaValue, equalManaValue, higherManaValue);

        harness.castAndResolveSorcery(player1, 0, opponentPermanent.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(
                lowerManaValue.getId(), equalManaValue.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.minCount()).isZero();

        harness.handleMultipleCardsChosen(player1, List.of(equalManaValue.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(opponentPermanent.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(equalManaValue.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(lowerManaValue.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(lowerManaValue, higherManaValue)
                .doesNotContain(equalManaValue);
    }

    @Test
    void canDeclineOptionalFollowUpTarget() {
        Card opponentPermanent = new GrizzlyBears();
        Card ownPermanent = new LlanowarElves();
        prepare(opponentPermanent, ownPermanent);

        harness.castAndResolveSorcery(player1, 0, opponentPermanent.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(opponentPermanent.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(ownPermanent.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownPermanent);
    }

    @Test
    void cannotTargetNonPermanentOpponentCard() {
        Card opponentInstant = new Jailbreak();
        prepare(opponentInstant);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, opponentInstant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void returnsLowerManaValuePermanentButDoesNotOfferSorceries() {
        Card opponentPermanent = new GrizzlyBears();
        Card ownPermanent = new LlanowarElves();
        Card ownSorcery = new Jailbreak();
        prepare(opponentPermanent, ownPermanent, ownSorcery);

        harness.castAndResolveSorcery(player1, 0, opponentPermanent.getId());

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(ownPermanent.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownPermanent.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(ownSorcery);
    }

    @Test
    void cannotTargetOwnGraveyardForInitialReturn() {
        Card opponentPermanent = new GrizzlyBears();
        Card ownPermanent = new LlanowarElves();
        prepare(opponentPermanent, ownPermanent);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, ownPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void initialTargetLeavingGraveyardPreventsBothReturns() {
        Card opponentPermanent = new GrizzlyBears();
        Card ownPermanent = new LlanowarElves();
        prepare(opponentPermanent, ownPermanent);

        harness.castSorcery(player1, 0, opponentPermanent.getId());
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void followUpTargetLeavingGraveyardDoesNotUndoOpponentReturn() {
        Card opponentPermanent = new GrizzlyBears();
        Card ownPermanent = new LlanowarElves();
        prepare(opponentPermanent, ownPermanent);

        harness.castAndResolveSorcery(player1, 0, opponentPermanent.getId());
        harness.handleMultipleCardsChosen(player1, List.of(ownPermanent.getId()));
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void returnsOpponentPermanentWhenOwnGraveyardHasNoEligibleCards() {
        Card opponentPermanent = new GrizzlyBears();
        Card ownPermanent = new AirElemental();
        prepare(opponentPermanent, ownPermanent);

        harness.castAndResolveSorcery(player1, 0, opponentPermanent.getId());
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        if (choice != null) {
            assertThat(choice.validCardIds()).isEmpty();
            harness.handleMultipleCardsChosen(player1, List.of());
        }
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Air Elemental");
        harness.assertInGraveyard(player1, "Air Elemental");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void torporOrbSuppressesFollowUpWhenOpponentCreatureEnters() {
        Card opponentPermanent = new GrizzlyBears();
        Card ownPermanent = new LlanowarElves();
        prepare(opponentPermanent, ownPermanent);
        harness.addToBattlefield(player1, new TorporOrb());

        harness.castAndResolveSorcery(player1, 0, opponentPermanent.getId());

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void prepare(Card opponentPermanent, Card... ownGraveyardCards) {
        harness.setGraveyard(player2, List.of(opponentPermanent));
        harness.setGraveyard(player1, List.of(ownGraveyardCards));
        harness.setHand(player1, List.of(new Jailbreak()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
