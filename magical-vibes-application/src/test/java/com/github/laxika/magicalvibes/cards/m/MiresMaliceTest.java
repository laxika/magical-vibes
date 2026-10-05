package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CarrierThrall;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MiresMalice.class, Forest.class, CarrierThrall.class})
class MiresMaliceTest extends BaseCardTest {

    @Test
    void targetOpponentDiscardsTwoCards() {
        harness.setHand(player2, List.of(new CarrierThrall(), new CarrierThrall()));
        castNormallyAt(player2.getId());
        discardTwoCards();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
    }

    @Test
    void alternateCastDiscardsAndAwakensTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new MiresMalice()));
        harness.setHand(player2, List.of(new CarrierThrall(), new CarrierThrall()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(player2.getId(), land.getId()));
        harness.passBothPriorities();
        discardTwoCards();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(land.isPermanentlyAnimated()).isTrue();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.ELEMENTAL)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(land.getCard().hasType(CardType.LAND)).isTrue();
    }

    @Test
    void alternateCastRequiresAwakenTarget() {
        harness.setHand(player1, List.of(new MiresMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("additional targets");
    }

    @Test
    void opponentChoosesWhichTwoCardsToDiscard() {
        CarrierThrall kept = new CarrierThrall();
        CarrierThrall discarded = new CarrierThrall();
        Forest discardedLand = new Forest();
        harness.setHand(player2, List.of(kept, discarded, discardedLand));

        castNormallyAt(player2.getId());
        harness.handleCardChosen(player2, 2);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).containsExactly(kept);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(discardedLand, discarded);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void opponentWithOneCardDiscardsItAndResolutionCompletes() {
        CarrierThrall card = new CarrierThrall();
        harness.setHand(player2, List.of(card));

        castNormallyAt(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(card);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void normalCastDoesNotAwakenALand() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player2, List.of());

        castNormallyAt(player2.getId());

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.isCreature(gd, land)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void cannotTargetYourselfForDiscard() {
        harness.setHand(player1, List.of(new MiresMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void awakenCannotTargetAnOpponentsLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        prepareAwaken();

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(player2.getId(), land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void awakenCannotTargetANonlandYouControl() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new CarrierThrall());
        prepareAwaken();

        assertThatThrownBy(() -> gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(player2.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void awakenStillResolvesWhenOpponentHasNoCards() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player2, List.of());
        prepareAwaken();

        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(player2.getId(), land.getId()));
        harness.passBothPriorities();

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(3);
        assertThat(gqs.hasEffectiveSubtype(gd, land, CardSubtype.FOREST)).isTrue();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    void repeatedAwakenAddsCountersWithoutResettingThem() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player2, List.of());

        for (int i = 0; i < 2; i++) {
            prepareAwaken();
            gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                    List.of(player2.getId(), land.getId()));
            harness.passBothPriorities();
        }

        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
    }

    @Test
    void opponentStillDiscardsWhenAwakenLandLeavesBeforeResolution() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player2, List.of(new CarrierThrall(), new CarrierThrall()));
        prepareAwaken();
        gs.playCardWithAlternateCost(gd, player1, 0, 0, null, null,
                List.of(player2.getId(), land.getId()));
        gd.playerBattlefields.get(player1.getId()).remove(land);

        harness.passBothPriorities();
        discardTwoCards();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(land.isPermanentlyAnimated()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    private void prepareAwaken() {
        harness.setHand(player1, List.of(new MiresMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }

    private void castNormallyAt(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new MiresMalice()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    private void discardTwoCards() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);
    }
}
