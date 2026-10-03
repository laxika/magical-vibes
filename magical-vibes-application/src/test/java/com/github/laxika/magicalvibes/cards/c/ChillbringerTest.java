package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SauroformHybrid;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Chillbringer.class, SauroformHybrid.class})
class ChillbringerTest extends BaseCardTest {

    @Test
    void tapsAnOpponentsCreatureAndSkipsItsNextUntap() {
        Permanent opponentCreature = addCreatureReady(player2, new SauroformHybrid());
        castChillbringer(opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(opponentCreature.getSkipUntapCount()).isEqualTo(1);
    }

    @Test
    void cannotTargetAControllerCreature() {
        Permanent ownCreature = addCreatureReady(player1, new SauroformHybrid());

        harness.setHand(player1, List.of(new Chillbringer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void restrictionExpiresAfterTheOpponentsNextUntapStep() {
        Permanent target = addCreatureReady(player2, new SauroformHybrid());
        castChillbringer(target.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void alreadyTappedCreatureStillSkipsItsNextUntap() {
        Permanent target = addCreatureReady(player2, new SauroformHybrid());
        target.setTapped(true);
        castChillbringer(target.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void overlappingRestrictionsExpireDuringTheSameUntapStep() {
        Permanent target = addCreatureReady(player2, new SauroformHybrid());
        castChillbringer(target.getId());
        harness.passBothPriorities();
        castChillbringer(target.getId());
        harness.passBothPriorities();

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void triggerResolvesAfterChillbringerLeavesTheBattlefield() {
        Permanent target = addCreatureReady(player2, new SauroformHybrid());
        castChillbringer(target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Chillbringer"));
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void removedTargetDoesNotAffectAnotherCreature() {
        Permanent target = addCreatureReady(player2, new SauroformHybrid());
        Permanent other = addCreatureReady(player2, new SauroformHybrid());
        castChillbringer(target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        harness.passBothPriorities();

        assertThat(other.isTapped()).isFalse();
        other.setTapped(true);
        harness.performUntapStep(player2);
        assertThat(other.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Chillbringer");
    }

    @Test
    void targetBecomingControlledByChillbringersControllerMakesTheTriggerIllegal() {
        Permanent target = addCreatureReady(player2, new SauroformHybrid());
        castChillbringer(target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        target.setTapped(true);
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void resolvedRestrictionFollowsTheCreatureAfterAControllerChange() {
        Permanent target = addCreatureReady(player2, new SauroformHybrid());
        castChillbringer(target.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerBattlefields.get(player1.getId()).add(target);

        harness.performUntapStep(player2);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isTrue();
        harness.performUntapStep(player1);
        assertThat(target.isTapped()).isFalse();
    }

    @Test
    void canEnterWithoutAnOpposingCreatureToTarget() {
        Permanent ownCreature = addCreatureReady(player1, new SauroformHybrid());
        castChillbringer(null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chillbringer");
        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private void castChillbringer(UUID targetId) {
        harness.setHand(player1, List.of(new Chillbringer()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
    }

}
