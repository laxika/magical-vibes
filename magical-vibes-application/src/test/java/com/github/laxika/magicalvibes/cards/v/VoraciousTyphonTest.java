package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VoraciousTyphon.class, GrizzlyBears.class})
class VoraciousTyphonTest extends BaseCardTest {

    @Test
    void castingFromHandEntersWithoutCounters() {
        harness.setHand(player1, List.of(new VoraciousTyphon()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent typhon = findPermanent(player1, "Voracious Typhon");
        assertThat(typhon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void escapingExilesFourOtherCardsAndEntersWithThreeCounters() {
        VoraciousTyphon typhon = new VoraciousTyphon();
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        GrizzlyBears third = new GrizzlyBears();
        GrizzlyBears fourth = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(typhon, first, second, third, fourth));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castFromGraveyard(player1, 0, List.of(1, 2, 3, 4));
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(first, second, third, fourth);

        harness.passBothPriorities();

        Permanent escapedTyphon = findPermanent(player1, "Voracious Typhon");
        assertThat(escapedTyphon.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(escapedTyphon.getEffectivePower()).isEqualTo(7);
        assertThat(escapedTyphon.getEffectiveToughness()).isEqualTo(7);
    }

    @Test
    void escapeRequiresFourOtherCardsInTheGraveyard() {
        harness.setGraveyard(player1, List.of(
                new VoraciousTyphon(), new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        assertThatThrownBy(() -> harness.castFromGraveyard(player1, 0, List.of(1, 2, 3)))
                .isInstanceOf(IllegalStateException.class);
    }
}
