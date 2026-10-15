package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheLordOfPain.class, GrizzlyBears.class, Shock.class})
class TheLordOfPainTest extends BaseCardTest {

    @Test
    @DisplayName("The Lord of Pain stops opponents from gaining life")
    void stopsOpponentsFromGainingLife() {
        harness.addToBattlefield(player1, new TheLordOfPain());

        assertThat(gqs.canPlayerGainLife(gd, player1.getId())).isTrue();
        assertThat(gqs.canPlayerGainLife(gd, player2.getId())).isFalse();
    }

    @Test
    @DisplayName("The first spell each player casts each turn deals damage equal to its mana value")
    void triggersForEachPlayerIndependentlyAndOnlyOnFirstSpell() {
        harness.addToBattlefield(player1, new TheLordOfPain());
        harness.setHand(player2, List.of(new GrizzlyBears(), new Shock()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        int player1LifeBefore = gd.getLife(player1.getId());
        int player2LifeBefore = gd.getLife(player2.getId());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player1.getId());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.castAndResolveInstant(player2, 0, player1.getId());
        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore - 4);

        harness.passPriority(player2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        choice = gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(player1LifeBefore - 4);
        assertThat(gd.getLife(player2.getId())).isEqualTo(player2LifeBefore - 3);
    }

    @Test
    void doesNotTriggerForSecondSpellWhenFirstWasCastBeforeItEntered() {
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castAndResolveInstant(player1, 0, player2.getId());

        harness.addToBattlefield(player1, new TheLordOfPain());
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 16);
    }

    @Test
    void castingTheLordOfPainAsFirstSpellDoesNotTriggerItself() {
        harness.setHand(player1, List.of(new TheLordOfPain(), new Shock()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "The Lord of Pain");
        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 18);
    }

    @Test
    void firstSpellCountResetsOnTheNextPlayersTurn() {
        harness.addToBattlefield(player1, new TheLordOfPain());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 17);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.ensurePriority(player1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.assertLife(player2, 14);
    }
}
