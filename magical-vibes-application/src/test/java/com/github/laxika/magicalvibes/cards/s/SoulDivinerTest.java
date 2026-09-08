package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.cards.o.OathOfKaya;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoulDiviner.class, FountainOfYouth.class, Forest.class, GideonBlackblade.class, OathOfKaya.class})
class SoulDivinerTest extends BaseCardTest {

    @Test
    @DisplayName("Removes a counter from a creature you control and draws a card")
    void removesCounterFromCreatureAndDraws() {
        Permanent diviner = addReadyDiviner();
        diviner.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        activateDiviner();

        assertThat(diviner.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Can remove a counter from an artifact you control")
    void removesCounterFromArtifact() {
        addReadyDiviner();
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new FountainOfYouth());
        artifact.setCounterCount(CounterType.CHARGE, 1);

        activateDiviner();

        assertThat(artifact.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Can remove a counter from a land you control")
    void removesCounterFromLand() {
        addReadyDiviner();
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
        land.setCounterCount(CounterType.CHARGE, 1);

        activateDiviner();

        assertThat(land.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Can remove a counter from a planeswalker you control")
    void removesCounterFromPlaneswalker() {
        addReadyDiviner();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player1, new GideonBlackblade());
        planeswalker.setCounterCount(CounterType.LOYALTY, 1);

        activateDiviner();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isZero();
    }

    @Test
    @DisplayName("Cannot remove a counter from an enchantment")
    void rejectsEnchantment() {
        addReadyDiviner();
        Permanent enchantment = harness.addToBattlefieldAndReturn(player1, new OathOfKaya());
        enchantment.setCounterCount(CounterType.CHARGE, 1);

        assertThatThrownBy(this::activateDiviner)
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
        assertThat(enchantment.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    private Permanent addReadyDiviner() {
        Permanent diviner = new Permanent(new SoulDiviner());
        diviner.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(diviner);
        return diviner;
    }

    private void activateDiviner() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
    }
}
