package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({ArlinnThePacksHope.class, ArlinnTheMoonsFury.class, GrizzlyBears.class})
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
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(arlinn.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
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

        gd.spellsCastLastTurn.clear();
        advanceToUntap(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.NIGHT);
        assertThat(arlinn.isTransformed()).isTrue();
        assertThat(arlinn.getCard()).isInstanceOf(ArlinnTheMoonsFury.class);

        gd.spellsCastLastTurn.put(player1.getId(), 2);
        advanceToUntap(player1);

        assertThat(gd.dayNight).isEqualTo(DayNight.DAY);
        assertThat(arlinn.isTransformed()).isFalse();
        assertThat(arlinn.getCard()).isInstanceOf(ArlinnThePacksHope.class);
    }

    private Permanent addReadyFrontFace(Player player, int loyalty) {
        ArlinnThePacksHope card = new ArlinnThePacksHope();
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private Permanent addReadyBackFace(Player player, int loyalty) {
        ArlinnThePacksHope card = new ArlinnThePacksHope();
        Permanent permanent = new Permanent(card);
        permanent.setCounterCount(CounterType.LOYALTY, loyalty);
        permanent.setSummoningSick(false);
        permanent.setTransformed(true);
        permanent.setCard(card.getBackFaceCard());
        gd.playerBattlefields.get(player.getId()).add(permanent);
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        return permanent;
    }

    private void advanceToUntap(Player activePlayer) {
        harness.performUntapStep(activePlayer);
    }
}
