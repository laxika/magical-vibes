package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GatstafShepherd;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartWolf;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class NightpackAmbusherTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private long wolfTokenCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Wolf"))
                .count();
    }

    @Test
    @DisplayName("Other Wolves and Werewolves you control get +1/+1")
    void buffsOtherWolvesAndWerewolves() {
        harness.addToBattlefield(player1, new HeartWolf());
        harness.addToBattlefield(player1, new GatstafShepherd());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent wolf = findPermanent(player1, "Heart Wolf");
        Permanent werewolf = findPermanent(player1, "Gatstaf Shepherd");
        Permanent bear = findPermanent(player1, "Grizzly Bears");
        int wolfPower = gqs.getEffectivePower(gd, wolf);
        int wolfToughness = gqs.getEffectiveToughness(gd, wolf);
        int werewolfPower = gqs.getEffectivePower(gd, werewolf);
        int werewolfToughness = gqs.getEffectiveToughness(gd, werewolf);
        int bearPower = gqs.getEffectivePower(gd, bear);
        int bearToughness = gqs.getEffectiveToughness(gd, bear);

        harness.addToBattlefield(player1, new NightpackAmbusher());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(wolfPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(wolfToughness + 1);
        assertThat(gqs.getEffectivePower(gd, werewolf)).isEqualTo(werewolfPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, werewolf)).isEqualTo(werewolfToughness + 1);
        assertThat(gqs.getEffectivePower(gd, bear)).isEqualTo(bearPower);
        assertThat(gqs.getEffectiveToughness(gd, bear)).isEqualTo(bearToughness);
    }

    @Test
    @DisplayName("Does not boost an opposing Wolf")
    void doesNotBoostOpposingWolf() {
        harness.addToBattlefield(player2, new HeartWolf());

        Permanent wolf = findPermanent(player2, "Heart Wolf");
        int power = gqs.getEffectivePower(gd, wolf);
        int toughness = gqs.getEffectiveToughness(gd, wolf);

        harness.addToBattlefield(player1, new NightpackAmbusher());

        assertThat(gqs.getEffectivePower(gd, wolf)).isEqualTo(power);
        assertThat(gqs.getEffectiveToughness(gd, wolf)).isEqualTo(toughness);
    }

    @Test
    @DisplayName("Creates a 2/2 green Wolf token at your end step if you cast no spell")
    void createsWolfTokenWhenNoSpellWasCast() {
        harness.addToBattlefield(player1, new NightpackAmbusher());

        advanceToEndStep(player1);

        assertThat(wolfTokenCount()).isEqualTo(1);
        Permanent wolfToken = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken() && p.getCard().getName().equals("Wolf"))
                .findFirst()
                .orElseThrow();
        assertThat(wolfToken.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, wolfToken)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wolfToken)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not create a token if you cast a spell this turn")
    void doesNotCreateWolfTokenWhenSpellWasCast() {
        harness.addToBattlefield(player1, new NightpackAmbusher());
        gd.recordSpellCast(player1.getId(), new GrizzlyBears());

        advanceToEndStep(player1);

        assertThat(wolfTokenCount()).isZero();
    }
}
