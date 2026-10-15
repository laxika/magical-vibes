package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.ParanormalAnalyst;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnsettlingTwins.class, Forest.class, ParanormalAnalyst.class})
class UnsettlingTwinsTest extends BaseCardTest {

    @Test
    void entersAndManifestsOneOfTheTopTwoCards() {
        Card manifestedCard = new UnsettlingTwins();
        Card graveyardCard = new Forest();
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.castFromHand(player1, new UnsettlingTwins(), "{3}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void manifestsTheOnlyCardAndTurningItFaceUpDoesNotTriggerItsEnterAbility() {
        Card onlyCard = new UnsettlingTwins();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.enterBattlefieldAndReturn(player1, new UnsettlingTwins());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(onlyCard.getId()));

        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        assertThat(manifested.getCard()).isSameAs(onlyCard);
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(manifested));
        assertThat(manifested.isFaceDown()).isFalse();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canChooseTheSecondCardWithoutTouchingTheRestOfTheLibrary() {
        Card first = new UnsettlingTwins();
        Card second = new Forest();
        Card untouched = new Forest();
        harness.setLibrary(player1, List.of(first, second, untouched));
        harness.enterBattlefieldAndReturn(player1, new UnsettlingTwins());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(second.getId()));
        Permanent manifested = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isManifested).findFirst().orElseThrow();
        assertThat(manifested.getCard()).isSameAs(second);
        assertThat(manifested.isFaceDown()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(untouched);
        org.assertj.core.api.Assertions.assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(manifested)))
                .isInstanceOf(IllegalStateException.class);
        assertThat(manifested.isFaceDown()).isTrue();
    }

    @Test
    void usesOnlyTheEnteringCreaturesControllersLibrary() {
        Card manifestedCard = new Forest();
        Card graveyardCard = new UnsettlingTwins();
        Card opponentsCard = new Forest();
        harness.setLibrary(player1, List.of(opponentsCard));
        harness.setLibrary(player2, List.of(manifestedCard, graveyardCard));
        harness.enterBattlefieldAndReturn(player2, new UnsettlingTwins());
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);
        harness.handleMultipleCardsChosen(player2, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard() == manifestedCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(opponentsCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isManifested);
    }

    @Test
    void analystReturnsTheUnchosenCardAfterManifesting() {
        Card manifestedCard = new UnsettlingTwins();
        Card graveyardCard = new Forest();
        harness.addToBattlefield(player1, new ParanormalAnalyst());
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.enterBattlefieldAndReturn(player1, new UnsettlingTwins());
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard() == manifestedCard);
    }

    @Test
    void manifestDreadStillTriggersAnalystWithAnEmptyLibrary() {
        harness.addToBattlefield(player1, new ParanormalAnalyst());
        harness.setLibrary(player1, List.of());
        harness.enterBattlefieldAndReturn(player1, new UnsettlingTwins());
        harness.passBothPriorities();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(Permanent::isManifested);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard()).isInstanceOf(ParanormalAnalyst.class);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
