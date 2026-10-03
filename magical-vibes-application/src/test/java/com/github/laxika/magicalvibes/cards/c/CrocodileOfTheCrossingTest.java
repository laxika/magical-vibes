package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrocodileOfTheCrossing.class, AirElemental.class, GrizzlyBears.class})
class CrocodileOfTheCrossingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB puts a -1/-1 counter on a creature you control")
    void etbPutsCounterOnOwnCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());

        harness.setHand(player1, List.of(new CrocodileOfTheCrossing()));
        harness.addMana(player1, ManaColor.GREEN, 4);

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

        harness.setHand(player1, List.of(new CrocodileOfTheCrossing()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0, bearsId);
        harness.passBothPriorities(); // resolve creature spell → ETB on stack
        harness.passBothPriorities(); // resolve ETB trigger → 0/0, dies to SBA

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target a creature you don't control")
    void cannotTargetOpponentCreature() {
        UUID opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears()).getId();

        harness.setHand(player1, List.of(new CrocodileOfTheCrossing()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, opponentCreature))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("Entering as the only creature requires putting the counter on itself")
    void enteringAsOnlyCreatureTargetsItself() {
        Permanent crocodile = harness.enterBattlefieldAndReturn(player1, new CrocodileOfTheCrossing());

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds()).containsExactly(crocodile.getId());
        harness.handlePermanentChosen(player1, crocodile.getId());
        harness.passBothPriorities();

        assertThat(crocodile.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, crocodile)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, crocodile)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Haste allows attacking on the turn it enters after targeting itself")
    void canAttackOnTurnItEnters() {
        int opponentLife = gd.playerLifeTotals.get(player2.getId());
        Permanent crocodile = harness.enterBattlefieldAndReturn(player1, new CrocodileOfTheCrossing());
        harness.handlePermanentChosen(player1, crocodile.getId());
        harness.passBothPriorities();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLife - 4);
    }

    @Test
    @DisplayName("The counter ability fails if its target changes controllers before resolution")
    void targetMustStillBeControlledOnResolution() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent crocodile = harness.enterBattlefieldAndReturn(player1, new CrocodileOfTheCrossing());
        harness.handlePermanentChosen(player1, bears.getId());

        gd.playerBattlefields.get(player1.getId()).remove(bears);
        gd.playerBattlefields.get(player2.getId()).add(bears);
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(crocodile.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
