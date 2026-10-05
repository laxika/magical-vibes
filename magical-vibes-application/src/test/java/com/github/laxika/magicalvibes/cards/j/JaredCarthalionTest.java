package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.ChildOfAlara;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.y.YavimayaKavu;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JaredCarthalion.class, ChildOfAlara.class, GrizzlyBears.class, YavimayaKavu.class,
        MycosynthLattice.class})
class JaredCarthalionTest extends BaseCardTest {

    @Test
    @DisplayName("+1 creates an all-color Kavu with trample")
    void plusOneCreatesAllColorKavu() {
        Permanent jared = addReadyJared(player1, 4);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(jared.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        Permanent kavu = findPermanents(player1, "Kavu").getFirst();
        assertThat(kavu.getCard().getPower()).isEqualTo(3);
        assertThat(kavu.getCard().getToughness()).isEqualTo(3);
        assertThat(gqs.getEffectiveColors(gd, kavu)).containsExactlyInAnyOrder(
                CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED, CardColor.GREEN);
        assertThat(gqs.hasKeyword(gd, kavu, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("-3 puts each target's color count in +1/+1 counters on it")
    void minusThreeUsesEachTargetsColorCount() {
        Permanent jared = addReadyJared(player1, 5);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new YavimayaKavu());

        harness.activateAbilityWithMultiTargets(player1, 0, 1,
                List.of(bears.getId(), kavu.getId()));
        harness.passBothPriorities();

        assertThat(jared.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(kavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("-6 returns a multicolored card without the all-colors rider")
    void minusSixReturnsMulticoloredCardWithoutRider() {
        addReadyJared(player1, 6);
        Card kavu = new YavimayaKavu();
        harness.setGraveyard(player1, List.of(kavu));

        harness.activateAbility(player1, 0, 2, null, kavu.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Yavimaya Kavu");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("-6 draws and creates Treasures when the returned card is all colors")
    void minusSixAppliesAllColorsRider() {
        addReadyJared(player1, 6);
        Card child = new ChildOfAlara();
        harness.setGraveyard(player1, List.of(child));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, child.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Child of Alara");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    private Permanent addReadyJared(Player player, int loyalty) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new JaredCarthalion());
        perm.setCounterCount(CounterType.LOYALTY, loyalty);
        perm.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return perm;
    }

    @Test
    void minusThreeAllowsNoTargets() {
        Permanent jared = addReadyJared(player1, 5);

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of());
        harness.passBothPriorities();

        assertThat(jared.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void minusThreeAllowsOneAllColorCreature() {
        addReadyJared(player1, 5);
        Permanent child = harness.addToBattlefieldAndReturn(player1, new ChildOfAlara());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(child.getId()));
        harness.passBothPriorities();

        assertThat(child.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void minusThreeStillAffectsRemainingLegalTarget() {
        addReadyJared(player1, 5);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent kavu = harness.addToBattlefieldAndReturn(player2, new YavimayaKavu());

        harness.activateAbilityWithMultiTargets(player1, 0, 1, List.of(bears.getId(), kavu.getId()));
        gd.playerBattlefields.get(player2.getId()).remove(bears);
        harness.setExile(player2, List.of(bears.getCard()));
        harness.passBothPriorities();

        assertThat(kavu.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void minusThreeRejectsChoosingTheSameCreatureTwice() {
        addReadyJared(player1, 5);
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1, 0, 1, List.of(bears.getId(), bears.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusSixRejectsMonocoloredCard() {
        addReadyJared(player1, 6);
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(bears));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, null, bears.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusSixRejectsOpponentsGraveyard() {
        addReadyJared(player1, 6);
        Card child = new ChildOfAlara();
        harness.setGraveyard(player2, List.of(child));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, null, child.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusSixDoesNotApplyRiderWhenTargetLeavesGraveyard() {
        addReadyJared(player1, 6);
        Card child = new ChildOfAlara();
        harness.setGraveyard(player1, List.of(child));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        harness.activateAbility(player1, 0, 2, null, child.getId(), Zone.GRAVEYARD);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(child));
        harness.passBothPriorities();

        harness.assertNotInHand(player1, "Child of Alara");
        harness.assertNotInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    void minusSixCanReturnAnAllColorNoncreatureCard() {
        addReadyJared(player1, 7);
        Card otherJared = new JaredCarthalion();
        harness.setGraveyard(player1, List.of(otherJared));
        harness.setLibrary(player1, List.of(new JaredCarthalion()));

        harness.activateAbility(player1, 0, 2, null, otherJared.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertInHand(player1, "Jared Carthalion");
        harness.assertNotInGraveyard(player1, "Jared Carthalion");
        assertThat(findPermanents(player1, "Treasure")).hasSize(2);
    }

    @Test
    void minusSixRejectsCardMadeColorlessByMycosynthLattice() {
        addReadyJared(player1, 7);
        harness.addToBattlefield(player1, new MycosynthLattice());
        Card otherJared = new JaredCarthalion();
        harness.setGraveyard(player1, List.of(otherJared));

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, 2, null, otherJared.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void minusSixDoesNotReturnTargetThatBecomesColorlessBeforeResolution() {
        addReadyJared(player1, 7);
        Card otherJared = new JaredCarthalion();
        harness.setGraveyard(player1, List.of(otherJared));
        harness.setLibrary(player1, List.of(new JaredCarthalion()));

        harness.activateAbility(player1, 0, 2, null, otherJared.getId(), Zone.GRAVEYARD);
        harness.addToBattlefield(player2, new MycosynthLattice());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Jared Carthalion");
        harness.assertNotInHand(player1, "Jared Carthalion");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }
}
