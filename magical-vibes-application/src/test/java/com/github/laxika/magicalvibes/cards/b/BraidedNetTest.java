package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.d.DarksteelRelic;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BraidedNet.class, BraidedQuipu.class, DarksteelRelic.class, GrizzlyBears.class,
        HillGiant.class, LlanowarElves.class})
class BraidedNetTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three net counters and locks another nonland permanent while tapped")
    void entersAndLocksAnotherPermanent() {
        Permanent net = harness.addToBattlefieldAndReturn(player1, new BraidedNet());
        Permanent elves = harness.addToBattlefieldAndReturn(player1, new LlanowarElves());
        elves.setSummoningSick(false);

        assertThat(net.getCounterCount(CounterType.NET)).isEqualTo(3);
        harness.activateAbility(player1, 0, null, elves.getId());
        harness.passBothPriorities();

        assertThat(elves.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.tapPermanent(player1, 1))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be activated");
        assertThat(net.getCounterCount(CounterType.NET)).isEqualTo(2);
    }

    @Test
    @DisplayName("Craft returns Braided Quipu transformed")
    void craftsIntoBraidedQuipu() {
        Permanent net = harness.addToBattlefieldAndReturn(player1, new BraidedNet());
        Permanent material = harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(net);
        assertThat(gd.findExiledCard(material.getCard().getId())).isNotNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isTransformed() && permanent.getCard() instanceof BraidedQuipu);
    }

    @Test
    @DisplayName("Quipu draws for each artifact before going third from the top")
    void quipuDrawsAndReturnsToLibrary() {
        Permanent quipu = addTransformedQuipu();
        harness.addToBattlefieldAndReturn(player1, new DarksteelRelic());
        Card first = new GrizzlyBears();
        Card second = new HillGiant();
        Card third = new LlanowarElves();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, quipu.getOriginalCard());
    }

    private Permanent addTransformedQuipu() {
        BraidedNet front = new BraidedNet();
        Permanent quipu = new Permanent(front);
        quipu.setCard(front.getBackFaceCard());
        quipu.setTransformed(true);
        quipu.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(quipu);
        return quipu;
    }
}
