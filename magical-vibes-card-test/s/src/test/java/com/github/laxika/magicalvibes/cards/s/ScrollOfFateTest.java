package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrollOfFate.class, GrizzlyBears.class})
class ScrollOfFateTest extends BaseCardTest {

    @Test
    @DisplayName("Manifests the only card in hand without asking")
    void manifestsTheOnlyCardInHandUnasked() {
        Permanent scroll = harness.addToBattlefieldAndReturn(player1, new ScrollOfFate());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(scroll.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.isFaceDown()).isTrue();
                    assertThat(permanent.getCard()).isSameAs(bears);
                });
    }

    @Test
    @DisplayName("Does nothing when the hand is empty")
    void doesNothingWhenHandIsEmpty() {
        harness.addToBattlefield(player1, new ScrollOfFate());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    @DisplayName("Manifests only the chosen card as an untapped 2/2")
    void manifestsOnlyTheChosenCard() {
        harness.addToBattlefield(player1, new ScrollOfFate());
        ScrollOfFate unchosen = new ScrollOfFate();
        ScrollOfFate chosen = new ScrollOfFate();
        harness.setHand(player1, List.of(unchosen, chosen));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(unchosen);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .singleElement()
                .satisfies(permanent -> {
                    assertThat(permanent.getCard()).isSameAs(chosen);
                    assertThat(permanent.isFaceDown()).isTrue();
                    assertThat(permanent.isTapped()).isFalse();
                    assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(2);
                    assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("The hand choice is shown to its controller alone and the log names no card")
    void handChoiceIsPrivate() {
        harness.addToBattlefield(player1, new ScrollOfFate());
        GrizzlyBears bears = new GrizzlyBears();
        ScrollOfFate other = new ScrollOfFate();
        harness.setHand(player1, List.of(bears, other));

        harness.activateAbility(player1, 0, null, null);
        harness.clearMessages();
        harness.passBothPriorities();

        var choice = gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), other.getId());
        assertThat(harness.getConn2().getMessagesContaining(bears.getId().toString())).isEmpty();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        assertThat(gd.gameLog).noneMatch(log -> log.plainText().contains("Grizzly Bears"));
    }

    @Test
    @DisplayName("A manifested noncreature card can't be turned face up for its mana cost")
    void manifestedNoncreatureCannotTurnFaceUpForItsManaCost() {
        harness.addToBattlefield(player1, new ScrollOfFate());
        harness.setHand(player1, List.of(new ScrollOfFate()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("A manifested creature card turns face up for its mana cost without using the stack")
    void manifestedCreatureTurnsFaceUpByPayingItsManaCostWithoutUsingTheStack() {
        harness.addToBattlefield(player1, new ScrollOfFate());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.turnFaceUp(player1, 1);

        Permanent creature = gd.playerBattlefields.get(player1.getId()).get(1);
        assertThat(creature.isFaceDown()).isFalse();
        assertThat(creature.getCard()).isSameAs(bears);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("The manifest choice can't be declined while the hand has cards")
    void manifestChoiceCannotBeDeclinedWhenHandIsNotEmpty() {
        harness.addToBattlefield(player1, new ScrollOfFate());
        ScrollOfFate card = new ScrollOfFate();
        GrizzlyBears other = new GrizzlyBears();
        harness.setHand(player1, List.of(card, other));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(other);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard()).isSameAs(card));
    }

    @Test
    @DisplayName("A manifested creature card can't be turned face up without paying its mana cost")
    void manifestedCreatureCannotTurnFaceUpWithoutPayingItsManaCost() {
        harness.addToBattlefield(player1, new ScrollOfFate());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isFaceDown()).isTrue();
    }

    @Test
    @DisplayName("A tapped Scroll of Fate can't be activated again")
    void tappedScrollCannotActivateAgain() {
        harness.addToBattlefield(player1, new ScrollOfFate());
        ScrollOfFate first = new ScrollOfFate();
        harness.setHand(player1, List.of(first, new ScrollOfFate()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested).hasSize(1);
    }
}
