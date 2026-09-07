package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GuyInTheChair.class, GiantSpider.class, GrizzlyBears.class})
class GuyInTheChairTest extends BaseCardTest {

    @Test
    void tapsForManaOfAnyColor() {
        Permanent guy = addCreatureReady(player1, new GuyInTheChair());

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(guy.isTapped()).isTrue();
    }

    @Test
    void putsCounterOnTargetSpider() {
        Permanent guy = addCreatureReady(player1, new GuyInTheChair());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, spider.getId());
        harness.passBothPriorities();

        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(guy.isTapped()).isTrue();
    }

    @Test
    void cannotTargetNonSpiderCreature() {
        Permanent guy = addCreatureReady(player1, new GuyInTheChair());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(guy.isTapped()).isFalse();
    }

    @Test
    void counterAbilityRequiresSorcerySpeed() {
        Permanent guy = addCreatureReady(player1, new GuyInTheChair());
        Permanent spider = addCreatureReady(player1, new GiantSpider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, spider.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
        assertThat(guy.isTapped()).isFalse();
        assertThat(spider.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
