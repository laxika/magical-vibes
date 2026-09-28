package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GolgariThug;
import com.github.laxika.magicalvibes.cards.p.Putrefy;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({VigorMortis.class, GolgariThug.class, Putrefy.class})
class VigorMortisTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature without a counter when green mana was not spent")
    void returnsCreatureWithoutCounterWithoutGreenMana() {
        Card creature = new GolgariThug();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new VigorMortis()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(findPermanent(player1, creature.getName())
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Returns a creature with a +1/+1 counter when green mana was spent")
    void returnsCreatureWithCounterWithGreenMana() {
        Card creature = new GolgariThug();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new VigorMortis()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveSorcery(player1, 0, creature.getId());

        assertThat(findPermanent(player1, creature.getName())
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a non-creature card in a graveyard")
    void cannotTargetNonCreatureCard() {
        Card instant = new Putrefy();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new VigorMortis()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetCreatureInOpponentsGraveyard() {
        Card creature = new GolgariThug();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new VigorMortis()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
