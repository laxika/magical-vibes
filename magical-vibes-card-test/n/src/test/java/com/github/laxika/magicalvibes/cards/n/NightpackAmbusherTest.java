package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GatstafShepherd;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HeartWolf;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NightpackAmbusher.class, HeartWolf.class, GatstafShepherd.class, GrizzlyBears.class})
class NightpackAmbusherTest extends BaseCardTest {

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.END_STEP);
        resolveAllTriggers();
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

    @Test
    void doesNotBoostItself() {
        Permanent ambusher = harness.addToBattlefieldAndReturn(player1, new NightpackAmbusher());

        assertThat(gqs.getEffectivePower(gd, ambusher)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ambusher)).isEqualTo(4);
    }

    @Test
    void multipleAmbushersBoostEachOtherAndEachCreatesAToken() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new NightpackAmbusher());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new NightpackAmbusher());

        assertThat(gqs.getEffectivePower(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, first)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, second)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, second)).isEqualTo(5);

        advanceToEndStep(player1);

        assertThat(wolfTokenCount()).isEqualTo(2);
        gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken())
                .forEach(p -> {
                    assertThat(p.getCard().getColor()).isEqualTo(CardColor.GREEN);
                    assertThat(p.getCard().getSubtypes()).contains(CardSubtype.WOLF);
                    assertThat(gqs.getEffectivePower(gd, p)).isEqualTo(4);
                    assertThat(gqs.getEffectiveToughness(gd, p)).isEqualTo(4);
                });
    }

    @Test
    void doesNotTriggerOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new NightpackAmbusher());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(wolfTokenCount()).isZero();
    }

    @Test
    void castingAmbusherDuringOwnTurnPreventsItsEndStepTrigger() {
        harness.castFromHand(player1, new NightpackAmbusher(), "{2}{G}{G}");
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Nightpack Ambusher");
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(wolfTokenCount()).isZero();
    }

    @Test
    void castingSpellInResponsePreventsTokenAtResolution() {
        harness.addToBattlefield(player1, new NightpackAmbusher());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.castFromHand(player1, new NightpackAmbusher(), "{2}{G}{G}");
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(wolfTokenCount()).isZero();
    }

    @Test
    void opponentsSpellInResponseDoesNotPreventToken() {
        harness.addToBattlefield(player1, new NightpackAmbusher());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.castFromHand(player2, new NightpackAmbusher(), "{2}{G}{G}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Nightpack Ambusher");
        assertThat(wolfTokenCount()).isEqualTo(1);
        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().isToken())).isEmpty();
    }

    @Test
    void flashingAmbusherAfterEndStepBeginsDoesNotTriggerRetroactively() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.END_STEP);

        harness.castFromHand(player1, new NightpackAmbusher(), "{2}{G}{G}");
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Nightpack Ambusher");
        assertThat(gd.stack).isEmpty();
        assertThat(wolfTokenCount()).isZero();
    }
}
