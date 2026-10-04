package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.l.LilianaDeathWielder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesiccatedNaga.class, LilianaDeathWielder.class})
class DesiccatedNagaTest extends BaseCardTest {

    @Test
    @DisplayName("Drains 2 life from target opponent when controlling a Liliana planeswalker")
    void drainsOpponentWhenControllingLiliana() {
        Permanent naga = addReadyNaga(player1);
        addReadyLiliana(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, indexOf(naga), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Cannot activate without a Liliana planeswalker")
    void cannotActivateWithoutLiliana() {
        Permanent naga = addReadyNaga(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(naga), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        Permanent naga = addReadyNaga(player1);
        addReadyLiliana(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(naga), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target yourself")
    void cannotTargetSelf() {
        Permanent naga = addReadyNaga(player1);
        addReadyLiliana(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(naga), null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("An opponent's Liliana does not allow activation")
    void cannotActivateWithOnlyOpponentsLiliana() {
        Permanent naga = addReadyNaga(player1);
        addReadyLiliana(player2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, indexOf(naga), null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The ability resolves even if Liliana leaves after activation")
    void resolvesAfterLilianaLeaves() {
        Permanent naga = addReadyNaga(player1);
        Permanent liliana = addReadyLiliana(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, indexOf(naga), null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(liliana);
        gd.playerGraveyards.get(player1.getId()).add(liliana.getCard());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Naga can activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent naga = addReadyNaga(player1);
        naga.setSummoningSick(true);
        naga.tap();
        addReadyLiliana(player1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, indexOf(naga), null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        harness.assertLife(player2, 18);
    }

    private Permanent addReadyNaga(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new DesiccatedNaga());
        perm.setSummoningSick(false);
        return perm;
    }

    private Permanent addReadyLiliana(Player player) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, new LilianaDeathWielder());
        perm.setCounterCount(CounterType.LOYALTY, 4);
        perm.setSummoningSick(false);
        return perm;
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
