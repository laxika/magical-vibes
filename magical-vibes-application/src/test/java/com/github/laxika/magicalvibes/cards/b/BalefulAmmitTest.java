package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BalefulAmmit.class, AirElemental.class, GrizzlyBears.class})
class BalefulAmmitTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a -1/-1 counter on a creature you control")
    void etbPutsCounterOnOwnCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.setHand(player1, List.of(new BalefulAmmit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, elemental.getId());
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB trigger

        // Air Elemental (4/4) with one -1/-1 counter → 3/3.
        assertThat(elemental.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(elemental.getEffectivePower()).isEqualTo(3);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("A -1/-1 counter can shrink a small creature to death")
    void etbCanKillSmallCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        UUID bearsId = bears.getId();

        // Weaken the 2/2 to 1/1 first so a single -1/-1 counter is lethal.
        bears.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);

        harness.setHand(player1, List.of(new BalefulAmmit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0, bearsId);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB trigger → 0/0, dies to SBA

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature you don't control")
    void cannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentCreature = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new BalefulAmmit()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, opponentCreature))
                .isInstanceOf(IllegalStateException.class);
        Permanent ammit = findPermanent(player1, "Baleful Ammit");
        harness.handlePermanentChosen(player1, ammit.getId());
        harness.passBothPriorities();
        assertThat(ammit.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Can enter an empty battlefield and target itself")
    void canTargetItselfWithNoOtherCreatures() {
        harness.setHand(player1, List.of(new BalefulAmmit()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent ammit = findPermanent(player1, "Baleful Ammit");
        harness.handlePermanentChosen(player1, ammit.getId());
        harness.passBothPriorities();

        assertThat(ammit.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ammit)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ammit)).isEqualTo(2);
    }

    @Test
    @DisplayName("Lifelink gains life equal to combat damage after shrinking")
    void lifelinkUsesPowerAfterCounter() {
        Permanent ammit = addCreatureReady(player1, new BalefulAmmit());
        ammit.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(23);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("An ETB target that changes controllers becomes illegal")
    void targetChangingControllersReceivesNoCounter() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new BalefulAmmit());
        harness.setHand(player1, List.of(new BalefulAmmit()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castCreature(player1, 0, target.getId());
        harness.passBothPriorities();

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
    }
}
