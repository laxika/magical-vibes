package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TerraStomper;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpellContortion.class, GrizzlyBears.class, Shock.class, TerraStomper.class})
class SpellContortionTest extends BaseCardTest {

    @Test
    void countersTargetSpellAndDrawsForEachMultikickerPayment() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setLibrary(player2, List.of(new Shock(), new Shock()));
        harness.setHand(player2, List.of(new SpellContortion()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithRepeatedCosts(player2, 0, bears.getId(), List.of("{1}{U}", "{1}{U}"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    void targetSpellIsNotCounteredWhenItsControllerPays() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new SpellContortion()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstantWithRepeatedCosts(player2, 0, shock.getId(), List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
    }
    @Test
    void drawsKickerCardEvenWhenOpponentPays() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new SpellContortion()));
        harness.setLibrary(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstantWithRepeatedCosts(player2, 0, shock.getId(), List.of("{1}{U}"));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 18);
    }

    @Test
    void countersAndDrawsWhenOpponentDeclinesPayment() {
        Shock shock = new Shock();
        harness.setHand(player1, List.of(shock));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.setHand(player2, List.of(new SpellContortion()));
        harness.setLibrary(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstantWithRepeatedCosts(player2, 0, shock.getId(), List.of("{1}{U}"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Shock");
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void unkickedSpellCountersWithoutDrawing() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new SpellContortion()));
        harness.setLibrary(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithRepeatedCosts(player2, 0, bears.getId(), List.of());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void controllerMayPayEvenWhenTargetSpellCannotBeCountered() {
        TerraStomper stomper = new TerraStomper();
        harness.setHand(player1, List.of(stomper));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.setHand(player2, List.of(new SpellContortion()));
        harness.setLibrary(player2, List.of(new Shock(), new Shock()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithRepeatedCosts(player2, 0, stomper.getId(), List.of("{1}{U}"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Terra Stomper");
    }
}
