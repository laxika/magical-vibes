package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cursecatcher;
import com.github.laxika.magicalvibes.cards.m.MistmeadowSkulk;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellSyphon.class, Cursecatcher.class, MistmeadowSkulk.class, SteelOfTheGodhead.class})
class SpellSyphonTest extends BaseCardTest {

    /** Player2 casts Spell Syphon on player1's Mistmeadow Skulk. */
    private void castSyphonOnSkulk(MistmeadowSkulk skulk, int additionalMana) {
        harness.addMana(player1, ManaColor.WHITE, additionalMana);
        harness.castFromHand(player1, skulk, "{1}{W}");
        harness.setHand(player2, List.of(new SpellSyphon()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.passPriority(player1);
        harness.castInstant(player2, 0, skulk.getId());
    }

    @Test
    @DisplayName("Counters the spell when its controller can't pay the scaled cost")
    void countersWhenControllerCannotPay() {
        harness.addToBattlefield(player2, new Cursecatcher());
        harness.addToBattlefield(player2, new Cursecatcher()); // 2 blue permanents -> pay {2}

        MistmeadowSkulk skulk = new MistmeadowSkulk();

        castSyphonOnSkulk(skulk, 1); // 2 to cast, only 1 left over
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.assertInGraveyard(player1, "Mistmeadow Skulk");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cost equals the number of blue permanents you control (two -> {2})")
    void costScalesWithTwoBluePermanents() {
        harness.addToBattlefield(player2, new Cursecatcher());
        harness.addToBattlefield(player2, new Cursecatcher()); // 2 blue permanents -> pay {2}

        MistmeadowSkulk skulk = new MistmeadowSkulk();

        castSyphonOnSkulk(skulk, 2); // 2 to cast, 2 left over
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);

        harness.handleMayAbilityChosen(player1, true); // pay {2}
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);

        harness.passBothPriorities(); // resolve the (uncountered) Mistmeadow Skulk
        assertThat(harness.getPermanentId(player1, "Mistmeadow Skulk")).isNotNull();
    }

    @Test
    @DisplayName("Cost equals the number of blue permanents you control (one -> {1})")
    void costScalesWithOneBluePermanent() {
        harness.addToBattlefield(player2, new Cursecatcher()); // 1 blue permanent -> pay {1}

        MistmeadowSkulk skulk = new MistmeadowSkulk();

        castSyphonOnSkulk(skulk, 1); // 2 to cast, 1 left over
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true); // pay {1}
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(0);

        harness.passBothPriorities();
        assertThat(harness.getPermanentId(player1, "Mistmeadow Skulk")).isNotNull();
    }

    @Test
    @DisplayName("With no blue permanents the ransom is {0} and accepting it lets the spell resolve")
    void zeroBluePermanentsPaysNothing() {
        MistmeadowSkulk skulk = new MistmeadowSkulk();

        castSyphonOnSkulk(skulk, 0); // exactly enough to cast, no mana left
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, true); // pay {0}
        harness.passBothPriorities();

        harness.assertNotInGraveyard(player1, "Mistmeadow Skulk");
        assertThat(harness.getPermanentId(player1, "Mistmeadow Skulk")).isNotNull();
    }

    @Test
    @DisplayName("The controller may decline a {0} ransom and have the spell countered")
    void zeroBluePermanentsCanStillBeDeclined() {
        MistmeadowSkulk skulk = new MistmeadowSkulk();

        castSyphonOnSkulk(skulk, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mistmeadow Skulk");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ignores non-blue permanents when determining the cost")
    void ignoresNonBluePermanents() {
        harness.addToBattlefield(player2, new Cursecatcher());
        harness.addToBattlefield(player2, new MistmeadowSkulk());

        MistmeadowSkulk skulk = new MistmeadowSkulk();

        castSyphonOnSkulk(skulk, 1); // 1 blue and 1 white permanent -> pay {1}
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(harness.getPermanentId(player1, "Mistmeadow Skulk")).isNotNull();
    }

    @Test
    @DisplayName("Blue permanents controlled by the opponent do not increase the cost")
    void ignoresOpponentsBluePermanents() {
        harness.addToBattlefield(player1, new Cursecatcher());
        harness.addToBattlefield(player1, new Cursecatcher());
        harness.addToBattlefield(player2, new Cursecatcher());

        castSyphonOnSkulk(new MistmeadowSkulk(), 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mistmeadow Skulk");
    }

    @Test
    @DisplayName("Declining an affordable nonzero payment counters the spell without spending mana")
    void canDeclineAffordablePayment() {
        harness.addToBattlefield(player2, new Cursecatcher());

        castSyphonOnSkulk(new MistmeadowSkulk(), 1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Mistmeadow Skulk");
        harness.assertNotOnBattlefield(player1, "Mistmeadow Skulk");
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Blue permanents entering after casting increase the cost at resolution")
    void countsBluePermanentsAtResolution() {
        harness.addToBattlefield(player2, new Cursecatcher());

        castSyphonOnSkulk(new MistmeadowSkulk(), 1);
        harness.addToBattlefield(player2, new Cursecatcher());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Mistmeadow Skulk");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("A multicolored blue noncreature permanent contributes exactly one to the cost")
    void countsMulticoloredBlueEnchantmentOnce() {
        var creature = harness.addToBattlefieldAndReturn(player2, new MistmeadowSkulk());
        var aura = harness.addToBattlefieldAndReturn(player2, new SteelOfTheGodhead());
        aura.setAttachedTo(creature.getId());

        castSyphonOnSkulk(new MistmeadowSkulk(), 1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Mistmeadow Skulk");
    }
}
