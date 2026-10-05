package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CyberConversion;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MeTheImmortal.class, GrizzlyBears.class, CyberConversion.class})
class MeTheImmortalTest extends BaseCardTest {

    @Test
    void choosesAPlusOnePlusOneCounterAtBeginningOfCombat() {
        Permanent me = harness.addToBattlefieldAndReturn(player1, new MeTheImmortal());

        advanceToCombat();
        harness.handleListChoice(player1, "+1/+1 counter");
        passIfReady();

        assertThat(me.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, me)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, me)).isEqualTo(4);
    }

    @Test
    void choosesAKeywordCounterAtBeginningOfCombat() {
        Permanent me = harness.addToBattlefieldAndReturn(player1, new MeTheImmortal());

        advanceToCombat();
        harness.handleListChoice(player1, "menace counter");
        passIfReady();

        assertThat(me.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, me, Keyword.MENACE)).isTrue();
    }

    @Test
    void castsFromGraveyardByDiscardingTwoCardsAndKeepsCounters() {
        MeTheImmortal card = new MeTheImmortal();
        Permanent me = addCreatureReady(player1, card);
        me.setCounterCount(CounterType.MENACE, 1);

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, me));
        harness.setHand(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        addMana();

        harness.castFromGraveyardWithDiscards(player1, 0, 0, List.of(1));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst()
                .orElseThrow();
        assertThat(returned.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, returned, Keyword.MENACE)).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotCastFromGraveyardWithoutTwoCardsToDiscard() {
        harness.setGraveyard(player1, List.of(new MeTheImmortal()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        addMana();

        assertThatThrownBy(() -> harness.castFromGraveyardWithDiscards(player1, 0, 0, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Must discard exactly 2 cards");
    }


    @Test
    void choosesFirstStrikeCounter() {
        Permanent me = harness.addToBattlefieldAndReturn(player1, new MeTheImmortal());

        advanceToCombat();
        harness.handleListChoice(player1, "first strike counter");
        passIfReady();

        assertThat(me.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, me, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    void choosesVigilanceCounter() {
        Permanent me = harness.addToBattlefieldAndReturn(player1, new MeTheImmortal());

        advanceToCombat();
        harness.handleListChoice(player1, "vigilance counter");
        passIfReady();

        assertThat(me.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, me, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    void doesNotTriggerDuringOpponentsCombat() {
        Permanent me = harness.addToBattlefieldAndReturn(player1, new MeTheImmortal());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.passUntil(player2, TurnStep.BEGINNING_OF_COMBAT);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(me.getCounters()).isEmpty();
    }

    @Test
    void retainedCountersModifyPowerAndToughnessInGraveyard() {
        MeTheImmortal card = new MeTheImmortal();
        Permanent me = harness.addToBattlefieldAndReturn(player1, card);
        me.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, me));

        harness.assertInGraveyard(player1, "Me, the Immortal");
        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(5);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(5);
    }

    @Test
    void losesCountersWhenLeavingBattlefieldFaceDown() {
        MeTheImmortal card = new MeTheImmortal();
        Permanent me = harness.addToBattlefieldAndReturn(player1, card);
        me.setCounterCount(CounterType.MENACE, 1);
        harness.setHand(player1, List.of(new CyberConversion()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, me.getId());
        assertThat(me.isFaceDown()).isTrue();

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, me));
        harness.setHand(player1, List.of(new MeTheImmortal(), new MeTheImmortal()));
        addMana();
        harness.castFromGraveyardWithDiscards(player1, 0, 0, List.of(1));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.MENACE)).isZero();
        assertThat(gqs.hasKeyword(gd, returned, Keyword.MENACE)).isFalse();
    }

    @Test
    void clearsCountersWhenReturnedToHand() {
        harness.setHand(player1, List.of());
        MeTheImmortal card = new MeTheImmortal();
        Permanent me = harness.addToBattlefieldAndReturn(player1, card);
        me.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        me.setCounterCount(CounterType.VIGILANCE, 1);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToHand(gd, me));
        addMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst().orElseThrow();
        assertThat(returned.getCounters()).isEmpty();
    }

    @Test
    void retainsAllCounterTypesWhenCastAgainFromGraveyard() {
        MeTheImmortal card = new MeTheImmortal();
        Permanent me = harness.addToBattlefieldAndReturn(player1, card);
        me.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        me.setCounterCount(CounterType.FIRST_STRIKE, 1);
        me.setCounterCount(CounterType.VIGILANCE, 1);
        me.setCounterCount(CounterType.MENACE, 1);
        me.setCounterCount(CounterType.CHARGE, 3);
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, me));
        harness.setHand(player1, List.of(new MeTheImmortal(), new MeTheImmortal()));
        addMana();

        harness.castFromGraveyardWithDiscards(player1, 0, 0, List.of(1));
        harness.passBothPriorities();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() == card)
                .findFirst().orElseThrow();
        assertThat(returned.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(returned.getCounterCount(CounterType.FIRST_STRIKE)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.VIGILANCE)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.MENACE)).isEqualTo(1);
        assertThat(returned.getCounterCount(CounterType.CHARGE)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(5);
    }

    private void advanceToCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
        harness.passBothPriorities();
    }

    private void passIfReady() {
        if (!gd.interaction.isAwaitingInput()) {
            harness.passBothPriorities();
        }
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
