package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.k.KeldonTwilight;
import com.github.laxika.magicalvibes.cards.l.LashknifeBarrier;
import com.github.laxika.magicalvibes.cards.m.ManaCylix;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RootGreevil.class, LashknifeBarrier.class, KeldonTwilight.class, ManaCylix.class})
class RootGreevilTest extends BaseCardTest {

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
}
