package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.s.Shock;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BroughtBack.class, GreenwoodSentinel.class, Shock.class, Plains.class, Pacifism.class})
class BroughtBackTest extends BaseCardTest {

    @Test
    @DisplayName("Returns up to two eligible permanents tapped")
    void returnsUpToTwoPermanentsTapped() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent secondBears = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, bears);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, secondBears);
        });

        harness.setHand(player1, List.of(new BroughtBack()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(
                bears.getCard().getId(), secondBears.getCard().getId());

        harness.handleMultipleCardsChosen(player1, List.of(bears.getCard().getId(), secondBears.getCard().getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getId().equals(bears.getCard().getId())
                        || p.getCard().getId().equals(secondBears.getCard().getId()))
                .hasSize(2)
                .allMatch(Permanent::isTapped);
    }

    @Test
    @DisplayName("Does not return cards that were not put into the graveyard from the battlefield this turn")
    void excludesCardsNotPutThereFromBattlefieldThisTurn() {
        Card bears = new GreenwoodSentinel();
        Card shock = new Shock();
        harness.setGraveyard(player1, List.of(bears, shock));
        harness.setHand(player1, List.of(new BroughtBack()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(bears, shock);
    }

    @Test
    @DisplayName("May choose zero targets even when eligible cards exist")
    void mayChooseZeroTargets() {
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, sentinel));
        harness.setHand(player1, List.of(new BroughtBack()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sentinel.getCard());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("May return just one eligible land tapped and leave another card behind")
    void returnsOneLandTapped() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Plains());
        Permanent sentinel = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, sentinel);
        });
        harness.setHand(player1, List.of(new BroughtBack()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(land.getCard().getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard()).isEqualTo(land.getCard());
            assertThat(permanent.isTapped()).isTrue();
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(sentinel.getCard())
                .doesNotContain(land.getCard());
    }

    @Test
    @DisplayName("Returns the remaining legal target when another target leaves the graveyard")
    void returnsRemainingLegalTarget() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
        });
        harness.setHand(player1, List.of(new BroughtBack()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getCard().getId(), second.getCard().getId()));
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removeCardFromGraveyardById(gd, first.getCard().getId());
            harness.getPermanentRemovalService().addCardToHandFromGraveyard(
                    gd, player1.getId(), player1.getId(), first.getCard());
        });
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard()).isEqualTo(second.getCard());
            assertThat(permanent.isTapped()).isTrue();
        });
        assertThat(gd.playerHands.get(player1.getId())).contains(first.getCard());
    }

    @Test
    @DisplayName("Only offers cards in the caster's graveyard")
    void excludesOpponentsGraveyard() {
        Permanent own = harness.addToBattlefieldAndReturn(player1, new GreenwoodSentinel());
        Permanent opposing = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, own);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, opposing);
        });
        harness.setHand(player1, List.of(new BroughtBack()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(own.getCard().getId());
        harness.handleMultipleCardsChosen(player1, List.of(own.getCard().getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard()).isEqualTo(own.getCard()));
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(opposing.getCard());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Returns an Aura tapped attached to a chosen existing creature")
    void returnsAuraAttachedToChosenCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GreenwoodSentinel());
        Permanent aura = harness.addToBattlefieldAndReturn(player1, new Pacifism());
        aura.setAttachedTo(creature.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura));
        harness.setHand(player1, List.of(new BroughtBack()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(aura.getCard().getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handlePermanentChosen(player1, creature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement().satisfies(permanent -> {
            assertThat(permanent.getCard()).isEqualTo(aura.getCard());
            assertThat(permanent.getAttachedTo()).isEqualTo(creature.getId());
            assertThat(permanent.isTapped()).isTrue();
        });
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(aura.getCard());
    }
}
