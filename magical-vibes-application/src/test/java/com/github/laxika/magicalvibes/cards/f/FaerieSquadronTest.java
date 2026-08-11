package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class FaerieSquadronTest extends BaseCardTest {

    @Test
    void castWithoutKickerDoesNotPutOnCounters() {
        harness.setHand(player1, List.of(new FaerieSquadron()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent faerie = findFaerie();
        assertThat(faerie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castWithKickerEntersWithTwoPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new FaerieSquadron()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent faerie = findFaerie();
        assertThat(faerie.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void castWithKickerRequiresAdditionalFourMana() {
        harness.setHand(player1, List.of(new FaerieSquadron()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castKickedCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent findFaerie() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof FaerieSquadron)
                .findFirst()
                .orElseThrow();
    }
}
