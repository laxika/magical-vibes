package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheEverChangingDane.class, Clone.class, GrizzlyBears.class, LlanowarElves.class})
class TheEverChangingDaneTest extends BaseCardTest {

    @Test
    @DisplayName("Becomes a copy of the creature sacrificed to its ability")
    void becomesCopyOfSacrificedCreature() {
        Permanent dane = addCreatureReady(player1, new TheEverChangingDane());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(dane), null, null);
        harness.passBothPriorities();

        assertThat(dane.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(dane.getCard().getPower()).isEqualTo(2);
        assertThat(dane.getCard().getToughness()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Retains the copy ability")
    void retainsCopyAbility() {
        Permanent dane = addCreatureReady(player1, new TheEverChangingDane());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, battlefieldIndex(dane), null, null);
        harness.passBothPriorities();

        addCreatureReady(player1, new LlanowarElves());
        harness.activateAbility(player1, battlefieldIndex(dane), null, null);
        harness.passBothPriorities();

        assertThat(dane.getCard().getName()).isEqualTo("Llanowar Elves");
        assertThat(dane.getCard().getPower()).isEqualTo(1);
        assertThat(dane.getCard().getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Requires another creature to pay the activation cost")
    void requiresAnotherCreature() {
        Permanent dane = addCreatureReady(player1, new TheEverChangingDane());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(dane), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A creature copying the Dane retains the sacrifice ability after each change")
    void copiedDaneRetainsAbilityAfterEachChange() {
        Permanent dane = addCreatureReady(player1, new TheEverChangingDane());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, battlefieldIndex(dane), null, null);
        harness.passBothPriorities();

        harness.castFromHand(player1, new Clone(), "{3}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, dane.getId());
        Permanent copy = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent != dane)
                .findFirst().orElseThrow();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, battlefieldIndex(copy), null, null);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "The Ever-Changing 'Dane");

        addCreatureReady(player1, new LlanowarElves());
        harness.activateAbility(player1, battlefieldIndex(copy), null, null);
        harness.passBothPriorities();
        assertThat(copy.getCard().getName()).isEqualTo("Llanowar Elves");

        addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, battlefieldIndex(copy), null, null);
        harness.passBothPriorities();
        assertThat(copy.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    @Test
    @DisplayName("Copying retains the Dane's counters and tapped status without copying the sacrifice's")
    void retainsOwnCountersAndStatus() {
        Permanent dane = addCreatureReady(player1, new TheEverChangingDane());
        dane.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        dane.tap();
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(dane), null, null);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(dane.getCard().getName()).isEqualTo("The Ever-Changing 'Dane");
        harness.passBothPriorities();

        assertThat(dane.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(dane.getCard().getPower()).isEqualTo(2);
        assertThat(dane.getCard().getToughness()).isEqualTo(2);
        assertThat(dane.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(dane.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot sacrifice an opponent's creature")
    void cannotSacrificeOpponentsCreature() {
        Permanent dane = addCreatureReady(player1, new TheEverChangingDane());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(dane), null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Gains the sacrificed creature's mana ability")
    void gainsSacrificedCreaturesManaAbility() {
        Permanent dane = addCreatureReady(player1, new TheEverChangingDane());
        addCreatureReady(player1, new LlanowarElves());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(dane), null, null);
        harness.passBothPriorities();
        harness.tapPermanent(player1, battlefieldIndex(dane));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(dane.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can activate while summoning sick without a tap cost")
    void canActivateWhileSummoningSick() {
        Permanent dane = harness.addToBattlefieldAndReturn(player1, new TheEverChangingDane());
        dane.setSummoningSick(true);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(dane), null, null);
        harness.passBothPriorities();

        assertThat(dane.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(dane.isSummoningSick()).isTrue();
    }

    @Test
    @DisplayName("Each pending activation uses its own sacrificed creature snapshot")
    void pendingActivationsKeepSeparateSnapshots() {
        Permanent dane = addCreatureReady(player1, new TheEverChangingDane());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(dane), null, null);
        addCreatureReady(player1, new LlanowarElves());
        harness.activateAbility(player1, battlefieldIndex(dane), null, null);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.passBothPriorities();
        assertThat(dane.getCard().getName()).isEqualTo("Llanowar Elves");
        harness.passBothPriorities();
        assertThat(dane.getCard().getName()).isEqualTo("Grizzly Bears");
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
