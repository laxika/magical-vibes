package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SwordsToPlowshares;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AnimarSoulOfElements.class, GrizzlyBears.class, Divination.class,
        SwordsToPlowshares.class, DoomBlade.class})
class AnimarSoulOfElementsTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a creature spell puts a +1/+1 counter on Animar")
    void gainsCounterWhenCreatureSpellIsCast() {
        Permanent animar = harness.addToBattlefieldAndReturn(player1, new AnimarSoulOfElements());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                && entry.getCard().getName().equals("Animar, Soul of Elements"));

        harness.passBothPriorities();

        assertThat(animar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each +1/+1 counter reduces creature spells by one generic mana")
    void countersReduceCreatureSpellCost() {
        Permanent animar = harness.addToBattlefieldAndReturn(player1, new AnimarSoulOfElements());
        animar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getEntryType() == StackEntryType.CREATURE_SPELL
                && entry.getCard().getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Noncreature spells are not reduced by Animar")
    void noncreatureSpellsAreNotReduced() {
        Permanent animar = harness.addToBattlefieldAndReturn(player1, new AnimarSoulOfElements());
        animar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Animar has protection from white")
    void hasProtectionFromWhite() {
        Permanent animar = harness.addToBattlefieldAndReturn(player2, new AnimarSoulOfElements());
        harness.setHand(player1, List.of(new SwordsToPlowshares()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, animar.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    @DisplayName("Animar has protection from black")
    void hasProtectionFromBlack() {
        Permanent animar = harness.addToBattlefieldAndReturn(player2, new AnimarSoulOfElements());
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, animar.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("protection");
    }

    @Test
    void reductionDoesNotPayColoredMana() {
        Permanent animar = harness.addToBattlefieldAndReturn(player1, new AnimarSoulOfElements());
        animar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 10);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void newCounterCannotDiscountTheSpellThatTriggersIt() {
        Permanent animar = harness.addToBattlefieldAndReturn(player1, new AnimarSoulOfElements());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(animar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void enteringWithoutBeingCastDoesNotTriggerAnimar() {
        Permanent animar = harness.addToBattlefieldAndReturn(player1, new AnimarSoulOfElements());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gd.stack).isEmpty();
        assertThat(animar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentsAnimarDoesNotReduceOrTriggerForYourCreature() {
        Permanent animar = harness.addToBattlefieldAndReturn(player2, new AnimarSoulOfElements());
        animar.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(animar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void castingNoncreatureSpellDoesNotAddCounter() {
        Permanent animar = harness.addToBattlefieldAndReturn(player1, new AnimarSoulOfElements());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(animar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void animarDoesNotTriggerForItsOwnCasting() {
        harness.setHand(player1, List.of(new AnimarSoulOfElements()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).singleElement()
                .satisfies(animar -> assertThat(animar.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
    }
}
