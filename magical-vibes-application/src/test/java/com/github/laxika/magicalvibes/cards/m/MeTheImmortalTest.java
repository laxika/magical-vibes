package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed({MeTheImmortal.class, GrizzlyBears.class})
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

    private void advanceToCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
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
