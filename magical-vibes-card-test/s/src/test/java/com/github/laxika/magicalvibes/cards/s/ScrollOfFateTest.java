package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ScrollOfFate.class, GrizzlyBears.class})
class ScrollOfFateTest extends BaseCardTest {

    @Test
    void manifestsAChosenCardFromHand() {
        Permanent scroll = harness.addToBattlefieldAndReturn(player1, new ScrollOfFate());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

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
    void doesNothingWhenHandIsEmpty() {
        harness.addToBattlefield(player1, new ScrollOfFate());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(Permanent::isManifested);
    }

    @Test
    void manifestsOnlyTheChosenCard() {
        harness.addToBattlefield(player1, new ScrollOfFate());
        ScrollOfFate unchosen = new ScrollOfFate();
        ScrollOfFate chosen = new ScrollOfFate();
        harness.setHand(player1, List.of(unchosen, chosen));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

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
    void manifestedNoncreatureCannotTurnFaceUpForItsManaCost() {
        harness.addToBattlefield(player1, new ScrollOfFate());
        harness.setHand(player1, List.of(new ScrollOfFate()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isFaceDown()).isTrue();
    }

    @Test
    void manifestedCreatureTurnsFaceUpByPayingItsManaCostWithoutUsingTheStack() {
        harness.addToBattlefield(player1, new ScrollOfFate());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
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
    void manifestChoiceCannotBeDeclinedWhenHandIsNotEmpty() {
        harness.addToBattlefield(player1, new ScrollOfFate());
        ScrollOfFate card = new ScrollOfFate();
        harness.setHand(player1, List.of(card));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.getCard()).isSameAs(card));
    }

    @Test
    void manifestedCreatureCannotTurnFaceUpWithoutPayingItsManaCost() {
        harness.addToBattlefield(player1, new ScrollOfFate());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.turnFaceUp(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId()).get(1).isFaceDown()).isTrue();
    }

    @Test
    void tappedScrollCannotActivateAgain() {
        harness.addToBattlefield(player1, new ScrollOfFate());
        harness.setHand(player1, List.of(new ScrollOfFate(), new ScrollOfFate()));
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested).hasSize(1);
    }
}
