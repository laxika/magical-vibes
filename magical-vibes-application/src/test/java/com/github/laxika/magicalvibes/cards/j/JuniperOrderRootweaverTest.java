package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.c.CutDown;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JuniperOrderRootweaver.class, YavimayaCoast.class, CutDown.class})
class JuniperOrderRootweaverTest extends BaseCardTest {

    @Test
    void withoutKickerDoesNotPutCounterOnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JuniperOrderRootweaver());
        harness.setHand(player1, List.of(new JuniperOrderRootweaver()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void kickedPutsCounterOnTargetCreatureYouControl() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JuniperOrderRootweaver());
        harness.setHand(player1, List.of(new JuniperOrderRootweaver()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void kickedCannotTargetOpponentCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JuniperOrderRootweaver());
        harness.setHand(player1, List.of(new JuniperOrderRootweaver()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        Permanent rootweaver = findPermanent(player1, "Juniper Order Rootweaver");
        harness.handlePermanentChosen(player1, rootweaver.getId());
        resolveAllTriggers();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(rootweaver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void kickedCannotTargetNoncreaturePermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YavimayaCoast());
        harness.setHand(player1, List.of(new JuniperOrderRootweaver()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        Permanent rootweaver = findPermanent(player1, "Juniper Order Rootweaver");
        harness.handlePermanentChosen(player1, rootweaver.getId());
        resolveAllTriggers();
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(rootweaver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void kickedCanTargetItselfOnAnOtherwiseEmptyBattlefield() {
        harness.setHand(player1, List.of(new JuniperOrderRootweaver()));
        addKickedMana();

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();

        Permanent rootweaver = findPermanent(player1, "Juniper Order Rootweaver");
        assertThat(rootweaver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.handlePermanentChosen(player1, rootweaver.getId());
        resolveAllTriggers();

        assertThat(rootweaver.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void counterTriggerResolvesAfterRootweaverIsDestroyed() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JuniperOrderRootweaver());
        harness.setHand(player1, List.of(new JuniperOrderRootweaver()));
        harness.setHand(player2, List.of(new CutDown()));
        addKickedMana();
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent source = findPermanents(player1, "Juniper Order Rootweaver").stream()
                .filter(permanent -> !permanent.getId().equals(target.getId())).findFirst().orElseThrow();
        harness.handlePermanentChosen(player1, target.getId());
        harness.castInstant(player2, 0, source.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(source);
        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void destroyedTargetDoesNotRedirectCounterToRootweaver() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new JuniperOrderRootweaver());
        harness.setHand(player1, List.of(new JuniperOrderRootweaver()));
        harness.setHand(player2, List.of(new CutDown()));
        addKickedMana();
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.castInstant(player2, 0, target.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(findPermanent(player1, "Juniper Order Rootweaver")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void addKickedMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
