package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WillowFaerie;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellStutter.class, GrizzlyBears.class, WillowFaerie.class})
class SpellStutterTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the spell when its controller cannot pay the base {2} cost")
    void countersWhenControllerCannotPayBaseCost() {
        GrizzlyBears bears = castBearsWithMana(3);
        castSpellStutter(bears);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Adds {1} to the cost for each Faerie controlled")
    void faerieIncreasesCost() {
        harness.addToBattlefield(player2, new WillowFaerie());

        GrizzlyBears bears = castBearsWithMana(4);
        castSpellStutter(bears);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Allows the spell to resolve when its controller pays {2} plus {1} per Faerie")
    void controllerPaysFaerieAdjustedCost() {
        harness.addToBattlefield(player2, new WillowFaerie());

        GrizzlyBears bears = castBearsWithMana(5);
        castSpellStutter(bears);

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    private GrizzlyBears castBearsWithMana(int amount) {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, amount);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        return bears;
    }

    private void castSpellStutter(GrizzlyBears bears) {
        harness.setHand(player2, List.of(new SpellStutter()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castInstant(player2, 0, bears.getId());
    }
}
