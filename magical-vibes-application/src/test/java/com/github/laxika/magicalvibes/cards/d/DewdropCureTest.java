package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.b.BraveKinDuo;
import com.github.laxika.magicalvibes.cards.b.BarkformHarvester;
import com.github.laxika.magicalvibes.cards.c.CarrotCake;
import com.github.laxika.magicalvibes.cards.l.LifecreedDuo;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DewdropCure.class, BraveKinDuo.class, BarkformHarvester.class, LifecreedDuo.class, CarrotCake.class})
class DewdropCureTest extends BaseCardTest {

    @Test
    void withoutGiftReturnsUpToTwoEligibleCreatures() {
        Card first = new BraveKinDuo();
        Card second = new BraveKinDuo();
        Card third = new BraveKinDuo();
        Card tooExpensive = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(first, second, third, tooExpensive));
        cast(List.of(first.getId(), second.getId()), false);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(third.getId(), tooExpensive.getId());
    }

    @Test
    void giftMakesOpponentDrawAndReturnsUpToThreeEligibleCreatures() {
        Card first = new BraveKinDuo();
        Card second = new BraveKinDuo();
        Card third = new BraveKinDuo();
        Card tooExpensive = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(first, second, third, tooExpensive));
        int opponentHandSize = gd.playerHands.get(player2.getId()).size();
        cast(List.of(first.getId(), second.getId(), third.getId()), true);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(first.getId(), second.getId(), third.getId());
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId).contains(tooExpensive.getId());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSize + 1);
    }

    @Test
    void withoutGiftCannotChooseThreeTargets() {
        Card first = new BraveKinDuo();
        Card second = new BraveKinDuo();
        Card third = new BraveKinDuo();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new DewdropCure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorceryWithGift(
                player1, 0, List.of(first.getId(), second.getId(), third.getId()), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must target between 0 and 2 targets");
    }

    private void cast(List<java.util.UUID> targetIds, boolean giftPromised) {
        harness.setHand(player1, List.of(new DewdropCure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorceryWithGift(player1, 0, targetIds, giftPromised);
        harness.passBothPriorities();
    }

    @Test
    void zeroTargetsWithoutGiftDoesNotMakeOpponentDraw() {
        int handSize = gd.playerHands.get(player2.getId()).size();
        cast(List.of(), false);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);
        harness.assertInGraveyard(player1, "Dewdrop Cure");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void zeroTargetsWithGiftStillMakesOpponentDraw() {
        int handSize = gd.playerHands.get(player2.getId()).size();
        cast(List.of(), true);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize + 1);
        harness.assertInGraveyard(player1, "Dewdrop Cure");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void returnsManaValueTwoCreatureUntappedWithoutGift() {
        Card creature = new LifecreedDuo();
        harness.setGraveyard(player1, List.of(creature));
        int handSize = gd.playerHands.get(player2.getId()).size();
        cast(List.of(creature.getId()), false);

        harness.assertOnBattlefield(player1, "Lifecreed Duo");
        harness.assertNotInGraveyard(player1, "Lifecreed Duo");
        assertThat(gd.playerBattlefields.get(player1.getId())).allMatch(p -> !p.isTapped());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);
    }

    @Test
    void giftDoesNotHappenWhenEveryTargetLeavesGraveyard() {
        Card creature = new BraveKinDuo();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new DewdropCure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorceryWithGift(player1, 0, List.of(creature.getId()), true);
        harness.setGraveyard(player1, List.of());
        int handSize = gd.playerHands.get(player2.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize);
        harness.assertNotOnBattlefield(player1, "Brave-Kin Duo");
        harness.assertInGraveyard(player1, "Dewdrop Cure");
    }

    @Test
    void remainingLegalTargetReturnsAndGiftIsGiven() {
        Card first = new BraveKinDuo();
        Card second = new BraveKinDuo();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new DewdropCure()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorceryWithGift(player1, 0, List.of(first.getId(), second.getId()), true);
        harness.setGraveyard(player1, List.of(second));
        int handSize = gd.playerHands.get(player2.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(p -> p.getCard().getId()).containsExactly(second.getId());
        assertThat(gd.playerHands.get(player2.getId())).hasSize(handSize + 1);
    }

    @Test
    void rejectsCreatureAboveManaValueLimit() {
        Card creature = new BarkformHarvester();
        harness.setGraveyard(player1, List.of(creature));
        assertThatThrownBy(() -> cast(List.of(creature.getId()), true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsNoncreatureCard() {
        Card artifact = new CarrotCake();
        harness.setGraveyard(player1, List.of(artifact));
        assertThatThrownBy(() -> cast(List.of(artifact.getId()), false))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsOpponentGraveyard() {
        Card creature = new BraveKinDuo();
        harness.setGraveyard(player2, List.of(creature));
        assertThatThrownBy(() -> cast(List.of(creature.getId()), true))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void rejectsRepeatedTarget() {
        Card creature = new BraveKinDuo();
        harness.setGraveyard(player1, List.of(creature));
        assertThatThrownBy(() -> cast(List.of(creature.getId(), creature.getId()), true))
                .isInstanceOf(IllegalStateException.class);
    }
}
