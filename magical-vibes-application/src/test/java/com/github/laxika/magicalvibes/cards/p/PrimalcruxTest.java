package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AltarOfThePantheon;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.Nightmare;
import com.github.laxika.magicalvibes.cards.o.OverbeingOfMyth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Primalcrux.class, GrizzlyBears.class, Nightmare.class, OverbeingOfMyth.class,
        AltarOfThePantheon.class})
class PrimalcruxTest extends BaseCardTest {

    @Test
    @DisplayName("Alone, Primalcrux is 6/6 from its own {G}{G}{G}{G}{G}{G} cost")
    void countsItsOwnGreenPips() {
        Permanent perm = addPrimalcruxReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(6);
    }

    @Test
    @DisplayName("Green pips on other permanents you control are added (6 + 1 = 7)")
    void addsGreenPipsOfOtherOwnPermanents() {
        Permanent perm = addPrimalcruxReady(player1);
        addPermanent(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(7);
    }

    @Test
    @DisplayName("Permanents without green mana symbols contribute nothing")
    void ignoresNonGreenPermanents() {
        Permanent perm = addPrimalcruxReady(player1);
        addPermanent(player1, new Nightmare());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(6);
    }

    @Test
    @DisplayName("Only permanents you control count, not the opponent's")
    void countsOnlyControllerPermanents() {
        Permanent perm = addPrimalcruxReady(player1);
        addPermanent(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(6);
    }

    @Test
    @DisplayName("P/T updates when another green permanent enters")
    void ptUpdatesWhenGreenPermanentAdded() {
        Permanent perm = addPrimalcruxReady(player1);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(6);

        addPermanent(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(7);
    }

    @Test
    void hybridSymbolsEachCountOnce() {
        Permanent primalcrux = harness.addToBattlefieldAndReturn(player1, new Primalcrux());
        harness.addToBattlefield(player1, new OverbeingOfMyth());

        assertThat(gqs.getEffectivePower(gd, primalcrux)).isEqualTo(11);
        assertThat(gqs.getEffectiveToughness(gd, primalcrux)).isEqualTo(11);
    }

    @Test
    void updatesWhenAnotherPrimalcruxLeaves() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Primalcrux());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new Primalcrux());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(12);

        gd.playerBattlefields.get(player1.getId()).remove(second);
        harness.setGraveyard(player1, List.of(second.getCard()));

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(6);
    }

    @Test
    void characteristicAbilityWorksInHandAndGraveyardWithoutCountingItself() {
        Primalcrux card = new Primalcrux();
        harness.setHand(player1, List.of(card));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isZero();
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isZero();

        harness.addToBattlefield(player1, new OverbeingOfMyth());
        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(5);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(5);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(card));
        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(5);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(5);
    }

    @Test
    void countersApplyAfterCharacteristicPowerAndToughness() {
        Permanent primalcrux = harness.addToBattlefieldAndReturn(player1, new Primalcrux());
        primalcrux.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        assertThat(gqs.getEffectivePower(gd, primalcrux)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, primalcrux)).isEqualTo(8);
    }

    @Test
    void devotionBonusesDoNotAddManaSymbols() {
        Permanent primalcrux = harness.addToBattlefieldAndReturn(player1, new Primalcrux());
        harness.addToBattlefield(player1, new AltarOfThePantheon());

        assertThat(gqs.getEffectivePower(gd, primalcrux)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, primalcrux)).isEqualTo(6);
    }

    private Permanent addPrimalcruxReady(Player player) {
        return addPermanent(player, new Primalcrux());
    }

    private Permanent addPermanent(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }
}
