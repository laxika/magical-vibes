package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.s.SnarlingWolf;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.DayNight;
import com.github.laxika.magicalvibes.model.Keyword;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArlinnThePacksHope.class, ArlinnTheMoonsFury.class, SnarlingWolf.class})
class ArlinnThePacksHopeTest extends BaseCardTest {

    @Test
    @DisplayName("+1 permits creature spells during the intervening turn and adds a counter")
    void plusOneGrantsFlashAndAdditionalCounterUntilNextTurn() {
        Permanent arlinn = addReadyFrontFace(player1, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new SnarlingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent wolf = findPermanent(player1, "Snarling Wolf");
        assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(wolf.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("−3 creates two Wolf tokens")
    void minusThreeCreatesTwoWolves() {
        Permanent arlinn = addReadyFrontFace(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(
                permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getSubtypes().contains(CardSubtype.WOLF))
                .hasSize(2);
    }

    @Test
    @DisplayName("The Moon's Fury adds red and green mana")
    void backFaceAddsRedAndGreenMana() {
        Permanent arlinn = addReadyBackFace(player1, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(5);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("The Moon's Fury becomes a non-planeswalker Werewolf until end of turn")
    void backFaceBecomesWerewolfCreature() {
        Permanent arlinn = addReadyBackFace(player1, 3);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, arlinn)).isTrue();
        assertThat(gqs.isPlaneswalker(gd, arlinn)).isFalse();
        assertThat(gqs.getEffectivePower(gd, arlinn)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, arlinn)).isEqualTo(5);
        assertThat(gqs.effectiveCreatureSubtypes(gd, arlinn)).contains(CardSubtype.WEREWOLF);
        assertThat(gqs.hasKeyword(gd, arlinn, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, arlinn, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, arlinn, Keyword.HASTE)).isTrue();
    }

    @Test
    void dayAndNightTransformArlinn() {
        Permanent arlinn = harness.enterBattlefieldAndReturn(player1, new ArlinnThePacksHope());

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(arlinn.isTransformed()).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(arlinn.isTransformed()).isTrue();
        assertThat(arlinn.getCard()).isInstanceOf(ArlinnTheMoonsFury.class);

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new SnarlingWolf(), new SnarlingWolf()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(arlinn.isTransformed()).isFalse();
        assertThat(arlinn.getCard()).isInstanceOf(ArlinnThePacksHope.class);
    }

    @Test
    void remainsDayWhenPreviousActivePlayerCastOneSpell() {
        Permanent arlinn = harness.enterBattlefieldAndReturn(player1, new ArlinnThePacksHope());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SnarlingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(arlinn.isTransformed()).isFalse();
    }

    @Test
    void entersWithBackFaceUpAtNight() {
        gd.dayNight = DayNight.NIGHT;

        Permanent arlinn = harness.enterBattlefieldAndReturn(player1, new ArlinnThePacksHope());

        assertThat(arlinn.isTransformed()).isTrue();
        assertThat(arlinn.getCard()).isInstanceOf(ArlinnTheMoonsFury.class);
        assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    void plusOneAppliesToTokensAfterSourceLeavesButNotOpposingCreatures() {
        addReadyFrontFace(player1, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).clear();

        Permanent ownCreature = harness.enterBattlefieldAndReturn(player1, new SnarlingWolf());
        Permanent opposingCreature = harness.enterBattlefieldAndReturn(player2, new SnarlingWolf());
        addReadyFrontFace(player1, 4);
        harness.activateAbility(player1, 1, 1, null, null);
        harness.passBothPriorities();

        assertThat(ownCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(opposingCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(permanent -> {
                    assertThat(permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
                    assertThat(gqs.getEffectivePower(gd, permanent)).isEqualTo(3);
                    assertThat(gqs.getEffectiveToughness(gd, permanent)).isEqualTo(3);
                });
    }

    @Test
    void plusOneExpiresAtStartOfControllersNextTurn() {
        addReadyFrontFace(player1, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passUntil(player2, TurnStep.UPKEEP);
        Permanent interveningCreature = harness.enterBattlefieldAndReturn(player1, new SnarlingWolf());
        assertThat(interveningCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.setHand(player2, List.of());
        harness.passUntil(player1, TurnStep.UPKEEP);
        Permanent nextTurnCreature = harness.enterBattlefieldAndReturn(player1, new SnarlingWolf());

        assertThat(nextTurnCreature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.setHand(player1, List.of(new SnarlingWolf()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void animationExpiresAndPreservesLoyalty() {
        gd.dayNight = DayNight.NIGHT;
        Permanent arlinn = addReadyBackFace(player1, 3);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gqs.isCreature(gd, arlinn)).isFalse();
        assertThat(gqs.isPlaneswalker(gd, arlinn)).isTrue();
        assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, arlinn, Keyword.INDESTRUCTIBLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, arlinn, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, arlinn, Keyword.TRAMPLE)).isFalse();
    }

    private Permanent addReadyFrontFace(Player player, int loyalty) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new ArlinnThePacksHope());
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addReadyBackFace(Player player, int loyalty) {
        ArlinnThePacksHope card = new ArlinnThePacksHope();
        Permanent permanent = harness.addToBattlefieldAndReturn(player, card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        permanent.setTransformed(true);
        permanent.setCard(card.getBackFaceCard());
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }
}
