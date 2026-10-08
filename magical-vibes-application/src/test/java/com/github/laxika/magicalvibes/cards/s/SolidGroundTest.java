package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AfiyaGrove;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SolidGround.class, Forest.class, AfiyaGrove.class})
class SolidGroundTest extends BaseCardTest {

    @Test
    void earthbendsTargetLandWithAnAdditionalCounter() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new SolidGround(), "{3}{G}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).containsExactly(land.getId());
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, land)).isTrue();
        assertThat(gqs.isLand(gd, land)).isTrue();
        assertThat(gqs.hasKeyword(gd, land, Keyword.HASTE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, land)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, land)).isEqualTo(4);
        assertThat(land.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void addsAnAdditionalCounterToAControlledNoncreaturePermanent() {
        harness.addToBattlefield(player1, new SolidGround());
        harness.castFromHand(player1, new AfiyaGrove(), "{1}{G}");
        harness.passBothPriorities();

        Permanent grove = findPermanent(player1, "Afiya Grove");
        assertThat(grove.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }

    @Test
    void cannotTargetAnOpponentsLand() {
        harness.addToBattlefield(player1, new Forest());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.castFromHand(player1, new SolidGround(), "{3}{G}");
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentLand.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void multipleCopiesEachAddOneCounterToTheSamePlacement() {
        harness.addToBattlefield(player1, new SolidGround());
        harness.addToBattlefield(player1, new SolidGround());

        harness.castFromHand(player1, new AfiyaGrove(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Afiya Grove").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(5);
    }

    @Test
    void doesNotAddCountersToAnOpponentsPermanent() {
        harness.addToBattlefield(player1, new SolidGround());
        harness.forceActivePlayer(player2);

        harness.castFromHand(player2, new AfiyaGrove(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Afiya Grove").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(3);
    }

    @Test
    void earthbendedLandReturnsTappedWithoutAnimationOrCountersAfterDying() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new SolidGround(), "{3}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, land));
        harness.assertInGraveyard(player1, "Forest");
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void earthbendedLandReturnsTappedWithoutAnimationOrCountersAfterExile() {
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.castFromHand(player1, new SolidGround(), "{3}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, land.getId());
        harness.passBothPriorities();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, land));
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Forest");
        assertThat(returned.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, returned)).isFalse();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.findExiledCard(land.getCard().getId())).isNull();
    }
}
