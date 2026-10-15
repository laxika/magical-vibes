package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SoulSummons;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MisthoofKirin.class, SoulSummons.class})
class MisthoofKirinTest extends BaseCardTest {

    @Test
    void megamorphPutsPlusOneCounterOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new MisthoofKirin()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent kirin = findPermanent(player1, "Misthoof Kirin");
        assertThat(kirin.isFaceDown()).isTrue();
        assertThat(kirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(kirin));

        assertThat(kirin.isFaceDown()).isFalse();
        assertThat(kirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void castingFaceUpDoesNotAddMegamorphCounter() {
        harness.setHand(player1, List.of(new MisthoofKirin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent kirin = findPermanent(player1, "Misthoof Kirin");
        assertThat(kirin.isFaceDown()).isFalse();
        assertThat(kirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void cannotPayMegamorphCostWithoutWhiteMana() {
        harness.setHand(player1, List.of(new MisthoofKirin()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent kirin = findPermanent(player1, "Misthoof Kirin");
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.turnFaceUp(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(kirin)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(kirin.isFaceDown()).isTrue();
        assertThat(kirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void manifestedKirinGetsCounterOnlyWhenMegamorphCostIsPaid(boolean payMegamorphCost) {
        harness.setHand(player1, List.of(new SoulSummons()));
        harness.setLibrary(player1, List.of(new MisthoofKirin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        Permanent kirin = findPermanent(player1, "Misthoof Kirin");
        assertThat(kirin.isManifested()).isTrue();
        assertThat(kirin.isFaceDown()).isTrue();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(kirin));
        harness.handleListChoice(player1, payMegamorphCost ? "Morph cost" : "Mana cost");

        assertThat(kirin.isFaceDown()).isFalse();
        assertThat(kirin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(payMegamorphCost ? 1 : 0);
    }
}
