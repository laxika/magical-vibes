package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.Glimmerpost;
import com.github.laxika.magicalvibes.cards.w.WhiteLotusHideout;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZhaoTheMoonSlayer.class, Glimmerpost.class, Forest.class, WhiteLotusHideout.class})
class ZhaoTheMoonSlayerTest extends BaseCardTest {

    @Test
    @DisplayName("Nonbasic lands enter tapped, but basic lands do not")
    void nonbasicLandsEnterTappedAndBasicLandsDoNot() {
        harness.addToBattlefield(player1, new ZhaoTheMoonSlayer());
        playLandAgainstZhao(player2, new Glimmerpost());

        assertThat(findPermanent(player2, "Glimmerpost").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Basic lands do not enter tapped")
    void basicLandsDoNotEnterTapped() {
        harness.addToBattlefield(player1, new ZhaoTheMoonSlayer());
        playLandAgainstZhao(player2, new Forest());

        assertThat(findPermanent(player2, "Forest").isTapped()).isFalse();
    }

    @Test
    @DisplayName("A conqueror counter turns nonbasic lands into Mountains")
    void conquerorCounterTurnsNonbasicLandsIntoMountains() {
        Permanent glimmerpost = harness.addToBattlefieldAndReturn(player1, new Glimmerpost());
        Permanent zhao = addZhaoReady(player1);
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(zhao.getCounterCount(CounterType.CONQUEROR)).isEqualTo(1);
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(glimmerpost.isTapped()).isTrue();
    }

    @Test
    void landTypesChangeForBothPlayersOnlyWhileCounterIsPresent() {
        Permanent ownLand = harness.addToBattlefieldAndReturn(player1, new WhiteLotusHideout());
        Permanent opposingLand = harness.addToBattlefieldAndReturn(player2, new WhiteLotusHideout());
        Permanent basicLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        Permanent zhao = harness.addToBattlefieldAndReturn(player1, new ZhaoTheMoonSlayer());
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThat(gqs.effectiveLandTypes(gd, ownLand)).isEmpty();
        harness.activateAbility(player1, 1, 0, null, null);
        assertThat(zhao.getCounterCount(CounterType.CONQUEROR)).isZero();
        assertThat(gqs.effectiveLandTypes(gd, ownLand)).isEmpty();
        harness.passBothPriorities();

        assertThat(gqs.effectiveLandTypes(gd, ownLand)).containsExactly(CardSubtype.MOUNTAIN);
        assertThat(gqs.effectiveLandTypes(gd, opposingLand)).containsExactly(CardSubtype.MOUNTAIN);
        assertThat(gqs.effectiveLandTypes(gd, basicLand)).containsExactly(CardSubtype.FOREST);
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        zhao.setCounterCount(CounterType.CONQUEROR, 0);
        assertThat(gqs.effectiveLandTypes(gd, ownLand)).isEmpty();
        assertThat(gqs.effectiveLandTypes(gd, opposingLand)).isEmpty();
        ownLand.untap();
        harness.tapPermanent(player1, 0);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
    }

    @Test
    void nonbasicLandStillEntersTappedWithCounterPresentAndLosesEnterTrigger() {
        Permanent zhao = harness.addToBattlefieldAndReturn(player1, new ZhaoTheMoonSlayer());
        zhao.setCounterCount(CounterType.CONQUEROR, 1);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        playLandAgainstZhao(player2, new Glimmerpost());
        resolveAllTriggers();

        Permanent land = findPermanent(player2, "Glimmerpost");
        assertThat(land.isTapped()).isTrue();
        assertThat(gqs.effectiveLandTypes(gd, land)).containsExactly(CardSubtype.MOUNTAIN);
        harness.assertLife(player2, lifeBefore);
    }

    @Test
    void controllersNonbasicLandAlsoEntersTappedWithoutCounter() {
        harness.addToBattlefield(player1, new ZhaoTheMoonSlayer());

        playLandAgainstZhao(player1, new WhiteLotusHideout());

        assertThat(findPermanent(player1, "White Lotus Hideout").isTapped()).isTrue();
    }

    @Test
    void counterAbilityCanBeActivatedRepeatedlyWhileTappedAndSummoningSick() {
        Permanent zhao = harness.addToBattlefieldAndReturn(player1, new ZhaoTheMoonSlayer());
        zhao.setSummoningSick(true);
        zhao.tap();
        prepareMainPhase();
        harness.addMana(player1, ManaColor.COLORLESS, 14);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(zhao.getCounterCount(CounterType.CONQUEROR)).isZero();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(zhao.getCounterCount(CounterType.CONQUEROR)).isEqualTo(2);
        assertThat(zhao.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    private Permanent addZhaoReady(Player player) {
        return addCreatureReady(player, new ZhaoTheMoonSlayer());
    }

    private void playLandAgainstZhao(Player player, com.github.laxika.magicalvibes.model.Card land) {
        harness.setHand(player, List.of(land));
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.playLand(player, 0);
    }

    private void prepareMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
