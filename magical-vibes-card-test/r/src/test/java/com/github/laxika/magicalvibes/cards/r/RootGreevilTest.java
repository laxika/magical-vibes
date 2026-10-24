package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.k.KalitasTraitorOfGhet;
import com.github.laxika.magicalvibes.cards.k.KeldonTwilight;
import com.github.laxika.magicalvibes.cards.l.LashknifeBarrier;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.cards.t.TheToymakersTrap;
import com.github.laxika.magicalvibes.cards.w.WheelOfSunAndMoon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.FailOnLegacyDepartureFallback;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RootGreevil.class, LashknifeBarrier.class, KeldonTwilight.class, ManaCylix.class})
class RootGreevilTest extends BaseCardTest {

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new RootGreevil());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Root Greevil");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotActivateWhileTapped() {
        addCreatureReady(player1, new RootGreevil()).tap();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Root Greevil");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotPayGreenCostWithOnlyColorlessMana() {
        addCreatureReady(player1, new RootGreevil());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Root Greevil");
        assertThat(findPermanent(player1, "Root Greevil").isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void choosingColorWithNoMatchingEnchantmentsStillResolves() {
        addCreatureReady(player1, new RootGreevil());
        harness.addToBattlefield(player2, new RootGreevil());
        harness.addToBattlefield(player2, new LashknifeBarrier());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        harness.assertInGraveyard(player1, "Root Greevil");
        harness.assertOnBattlefield(player2, "Root Greevil");
        harness.assertOnBattlefield(player2, "Lashknife Barrier");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Activating Root Greevil sacrifices it and prompts for a color")
    void activationSacrificesAndPromptsForColor() {
        addCreatureReady(player1, new RootGreevil());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Root Greevil");
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class).playerId())
                .isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Destroys only enchantments of the chosen color")
    void destroysOnlyEnchantmentsOfChosenColor() {
        addCreatureReady(player1, new RootGreevil());
        harness.addToBattlefield(player1, new LashknifeBarrier());
        harness.addToBattlefield(player2, new LashknifeBarrier());
        harness.addToBattlefield(player2, new KeldonTwilight());
        harness.addToBattlefield(player2, new ManaCylix());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        harness.assertInGraveyard(player1, "Root Greevil");
        harness.assertInGraveyard(player1, "Lashknife Barrier");
        harness.assertInGraveyard(player2, "Lashknife Barrier");
        harness.assertOnBattlefield(player2, "Keldon Twilight");
        harness.assertOnBattlefield(player2, "Mana Cylix");
    }

    @Test
    @DisplayName("Destroys a multicolored enchantment when its chosen color matches")
    void destroysMulticoloredEnchantmentOfChosenColor() {
        addCreatureReady(player1, new RootGreevil());
        harness.addToBattlefield(player2, new KeldonTwilight());
        harness.addToBattlefield(player2, new LashknifeBarrier());
        harness.addToBattlefield(player2, new ManaCylix());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.assertInGraveyard(player2, "Keldon Twilight");
        harness.assertOnBattlefield(player2, "Lashknife Barrier");
        harness.assertOnBattlefield(player2, "Mana Cylix");
    }

    @Test
    @CardUsed({KalitasTraitorOfGhet.class, Opalescence.class, WheelOfSunAndMoon.class, TheToymakersTrap.class})
    @FailOnLegacyDepartureFallback
    @DisplayName("The chosen color's enchantment creature is asked about its death replacement before it is destroyed")
    void asksTheDeathReplacementBeforeTheChosenColorsEnchantmentIsDestroyed() {
        addCreatureReady(player1, new RootGreevil());
        harness.addToBattlefield(player1, new KalitasTraitorOfGhet());
        harness.addToBattlefield(player2, new Opalescence());
        Permanent wheel = harness.addToBattlefieldAndReturn(player2, new WheelOfSunAndMoon());
        wheel.setAttachedTo(player2.getId());
        Permanent trap = harness.addToBattlefieldAndReturn(player2, new TheToymakersTrap());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        PendingInteraction.ColorChoice replacementChoice =
                gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(replacementChoice.playerId()).isEqualTo(player2.getId());
        assertThat(replacementChoice.options())
                .containsExactlyInAnyOrder("Put it on the bottom of its owner's library", "Kalitas, Traitor of Ghet");
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(trap);

        harness.handleListChoice(player2, "Put it on the bottom of its owner's library");

        harness.assertNotOnBattlefield(player2, "The Toymaker's Trap");
        assertThat(gd.playerDecks.get(player2.getId())).extracting(Card::getName).contains("The Toymaker's Trap");
        harness.assertNotOnBattlefield(player1, "Zombie");
        harness.assertOnBattlefield(player2, "Opalescence");
        harness.assertOnBattlefield(player2, "Wheel of Sun and Moon");
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
