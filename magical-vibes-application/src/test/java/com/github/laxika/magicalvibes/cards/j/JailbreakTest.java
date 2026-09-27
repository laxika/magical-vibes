package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Jailbreak.class, AirElemental.class, GrizzlyBears.class, LlanowarElves.class})
class JailbreakTest extends BaseCardTest {

    @Test
    void returnsOpponentPermanentAndThenOwnPermanentWithEqualOrLesserManaValue() {
        Card opponentPermanent = new GrizzlyBears();
        Card lowerManaValue = new LlanowarElves();
        Card equalManaValue = new GrizzlyBears();
        Card higherManaValue = new AirElemental();
        prepare(opponentPermanent, lowerManaValue, equalManaValue, higherManaValue);

        harness.castSorcery(player1, 0, opponentPermanent.getId());
        harness.passBothPriorities();

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

        harness.castSorcery(player1, 0, opponentPermanent.getId());
        harness.passBothPriorities();
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

    private void prepare(Card opponentPermanent, Card... ownGraveyardCards) {
        harness.setGraveyard(player2, List.of(opponentPermanent));
        harness.setGraveyard(player1, List.of(ownGraveyardCards));
        harness.setHand(player1, List.of(new Jailbreak()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
