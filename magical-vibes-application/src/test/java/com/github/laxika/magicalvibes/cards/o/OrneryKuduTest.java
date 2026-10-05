package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrneryKudu.class, AirElemental.class, GrizzlyBears.class})
class OrneryKuduTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a -1/-1 counter on a creature you control")
    void etbPutsCounterOnOwnCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.setHand(player1, List.of(new OrneryKudu()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0, elemental.getId());
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB trigger

        // Air Elemental (4/4) with one -1/-1 counter → 3/3.
        assertThat(elemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(elemental.getEffectivePower()).isEqualTo(3);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("ETB cannot target a creature you don't control")
    void etbCannotTargetOpponentCreature() {
        UUID opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new OrneryKudu()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, opponentCreature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Kudu can be cast onto an empty battlefield and target itself after entering")
    void castOntoEmptyBattlefieldTargetsItself() {
        harness.castFromHand(player1, new OrneryKudu(), "{2}{G}");
        harness.passBothPriorities();

        Permanent kudu = findPermanent(player1, "Ornery Kudu");
        assertThat(kudu.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.handlePermanentChosen(player1, kudu.getId());
        harness.passBothPriorities();

        assertThat(kudu.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, kudu)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, kudu)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Entering without being cast still puts a counter on a chosen creature")
    void noncastEntryPutsCounterOnChosenCreature() {
        Permanent otherKudu = harness.addToBattlefieldAndReturn(player1, new OrneryKudu());
        Permanent enteringKudu = harness.enterBattlefieldAndReturn(player1, new OrneryKudu());

        harness.handlePermanentChosen(player1, otherKudu.getId());
        harness.passBothPriorities();

        assertThat(otherKudu.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(enteringKudu.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A target leaving before resolution does not move the counter to Kudu")
    void removedTargetDoesNotRedirectCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OrneryKudu());
        Permanent enteringKudu = harness.enterBattlefieldAndReturn(player1, new OrneryKudu());
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerGraveyards.get(player1.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(enteringKudu.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
