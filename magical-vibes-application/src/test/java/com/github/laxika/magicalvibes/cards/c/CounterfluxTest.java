package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Counterflux.class, GrizzlyBears.class, MightOfOaks.class, Cancel.class})
class CounterfluxTest extends BaseCardTest {

    @Test
    @DisplayName("Counters target spell you don't control")
    void countersOpponentSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Counterflux()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Counterflux");
    }

    @Test
    @DisplayName("Cannot target a spell you control")
    void cannotTargetOwnSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears, new Counterflux()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("This spell can't be countered")
    void thisSpellCantBeCountered() {
        GrizzlyBears bears = new GrizzlyBears();
        Counterflux flux = new Counterflux();
        harness.setHand(player1, List.of(bears, new Cancel()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.setHand(player2, List.of(flux));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.castAndResolveInstant(player1, 0, flux.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Cancel");
        harness.assertInGraveyard(player2, "Counterflux");
    }

    @Test
    @DisplayName("Overloaded, counters each spell you don't control")
    void overloadCountersEachOpponentSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        MightOfOaks might = new MightOfOaks();
        Permanent battlefieldBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(bears, might));
        harness.addMana(player1, ManaColor.GREEN, 6);

        harness.setHand(player2, List.of(new Counterflux()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player1, 0, battlefieldBear.getId());
        harness.passPriority(player1);
        harness.castWithOverload(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Might of Oaks");
        harness.assertInGraveyard(player2, "Counterflux");
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Overloaded Counterflux does not counter your own spell on the stack")
    void overloadLeavesOwnSpellAlone() {
        GrizzlyBears opponentSpell = new GrizzlyBears();
        Permanent ownBear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(opponentSpell));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new MightOfOaks(), new Counterflux()));
        harness.addMana(player2, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, ownBear.getId());
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castWithOverload(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Counterflux");
        assertThat(gd.stack).isEmpty();
        assertThat(ownBear.getPowerModifier()).isEqualTo(7);
    }

    @Test
    @DisplayName("Overload requires the full overload cost")
    void overloadRequiresFullCost() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new Counterflux()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        assertThatThrownBy(() -> harness.castWithOverload(player2, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Overload can be cast with no other spells on the stack")
    void overloadWithEmptyStack() {
        harness.setHand(player1, List.of(new Counterflux()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castWithOverload(player1, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Counterflux");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Normal casting requires a target spell")
    void normalCastCannotOmitTarget() {
        harness.setHand(player1, List.of(new Counterflux()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.assertInHand(player1, "Counterflux");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Without overload, only the targeted opposing spell is countered")
    void normalCastLeavesOtherOpponentSpellAlone() {
        GrizzlyBears bears = new GrizzlyBears();
        MightOfOaks might = new MightOfOaks();
        Permanent battlefieldBear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(bears, might));
        harness.addMana(player1, ManaColor.GREEN, 6);
        harness.setHand(player2, List.of(new Counterflux()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.castInstant(player1, 0, battlefieldBear.getId());
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, might.getId());

        harness.assertInGraveyard(player1, "Might of Oaks");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.stack).hasSize(1);
        assertThat(battlefieldBear.getPowerModifier()).isZero();
        harness.passBothPriorities();
        assertThat(countPermanents(player1, "Grizzly Bears")).isEqualTo(2);
    }

    @Test
    @DisplayName("Overload leaves an opposing uncounterable spell on the stack")
    void overloadCannotCounterOpponentCounterflux() {
        GrizzlyBears bears = new GrizzlyBears();
        Counterflux opponentFlux = new Counterflux();
        Cancel cancel = new Cancel();
        harness.setHand(player1, List.of(bears, opponentFlux));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setHand(player2, List.of(cancel, new Counterflux()));
        harness.addMana(player2, ManaColor.BLUE, 4);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, bears.getId());
        harness.castInstant(player1, 0, cancel.getId());
        harness.castWithOverload(player2, 0);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Counterflux");
        harness.assertNotInGraveyard(player2, "Cancel");
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Counterflux");
        harness.assertInGraveyard(player2, "Cancel");
        assertThat(gd.stack).isEmpty();
    }
}
