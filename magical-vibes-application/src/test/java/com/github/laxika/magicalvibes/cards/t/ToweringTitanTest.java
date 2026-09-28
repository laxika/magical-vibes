package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CarnivorousPlant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ToweringTitan.class, CarnivorousPlant.class, GrizzlyBears.class})
class ToweringTitanTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with counters equal to the total toughness of other creatures you control")
    void entersWithOtherControlledCreaturesTotalToughness() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new CarnivorousPlant());
        harness.addToBattlefield(player2, new CarnivorousPlant());
        harness.setHand(player1, List.of(new ToweringTitan()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent titan = findPermanent(player1, "Towering Titan");
        assertThat(titan.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(7);
        assertThat(gqs.getEffectivePower(gd, titan)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, titan)).isEqualTo(7);
    }

    @Test
    @DisplayName("Sacrificing a creature with defender grants all creatures trample")
    void sacrificesDefenderAndGrantsTrampleToAllCreatures() {
        Permanent titan = addTitanWithCounter();
        Permanent defender = addCreatureReady(player1, new CarnivorousPlant());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Carnivorous Plant");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(defender);
        assertThat(gqs.hasKeyword(gd, titan, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("The team trample grant expires at end of turn")
    void trampleExpiresAtEndOfTurn() {
        Permanent titan = addTitanWithCounter();
        Permanent defender = addCreatureReady(player1, new CarnivorousPlant());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, titan, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, titan, Keyword.TRAMPLE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("The ability cannot be activated without a creature with defender")
    void requiresCreatureWithDefender() {
        addTitanWithCounter();
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("a creature with defender");
    }

    private Permanent addTitanWithCounter() {
        Permanent titan = addCreatureReady(player1, new ToweringTitan());
        titan.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        return titan;
    }
}
