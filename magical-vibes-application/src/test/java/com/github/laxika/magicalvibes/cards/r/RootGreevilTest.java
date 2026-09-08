package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.e.Exploration;
import com.github.laxika.magicalvibes.cards.g.GloriousAnthem;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.addToBattlefield(player2, new GloriousAnthem());
        harness.addToBattlefield(player2, new PhyrexianArena());
        harness.addToBattlefield(player2, new Exploration());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        harness.assertInGraveyard(player1, "Root Greevil");
        harness.assertInGraveyard(player2, "Glorious Anthem");
        harness.assertOnBattlefield(player2, "Phyrexian Arena");
        harness.assertOnBattlefield(player2, "Exploration");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
