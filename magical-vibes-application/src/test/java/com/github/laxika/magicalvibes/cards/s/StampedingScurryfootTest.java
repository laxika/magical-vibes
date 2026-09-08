package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StampedingScurryfootTest extends BaseCardTest {

    @Test
    @DisplayName("Exhaust puts a counter on Stampeding Scurryfoot and creates an Elephant")
    void exhaustPutsCounterAndCreatesElephant() {
        Permanent scurryfoot = addCreatureReady(player1, new StampedingScurryfoot());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(scurryfoot.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        Permanent elephant = findPermanent(player1, "Elephant");
        assertThat(elephant.getCard().isToken()).isTrue();
        assertThat(elephant.getCard().getPower()).isEqualTo(3);
        assertThat(elephant.getCard().getToughness()).isEqualTo(3);
        assertThat(elephant.getCard().getColor()).isEqualTo(CardColor.GREEN);
        assertThat(elephant.getCard().getSubtypes()).contains(CardSubtype.ELEPHANT);
    }

    @Test
    @DisplayName("Exhaust can be activated only once for the permanent")
    void exhaustCanBeActivatedOnlyOnce() {
        addCreatureReady(player1, new StampedingScurryfoot());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
