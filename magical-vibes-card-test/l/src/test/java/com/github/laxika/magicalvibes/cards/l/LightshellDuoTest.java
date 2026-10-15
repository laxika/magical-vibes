package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightshellDuo.class, GrizzlyBears.class, Shock.class})
class LightshellDuoTest extends BaseCardTest {

    @Test
    @DisplayName("When it enters, surveil 2")
    void surveilsTwoWhenItEnters() {
        Card topCard = new GrizzlyBears();
        Card bottomCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        harness.setHand(player1, List.of(new LightshellDuo()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard, bottomCard);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of(1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(bottomCard);
    }

    @Test
    @DisplayName("Casting a noncreature spell gives +1/+1 until end of turn")
    void noncreatureSpellPumps() {
        Permanent lightshell = addCreatureReady(player1, new LightshellDuo());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lightshell)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lightshell)).isEqualTo(5);
    }

    @Test
    @DisplayName("The prowess boost wears off at end of turn")
    void prowessBoostWearsOffAtEndOfTurn() {
        Permanent lightshell = addCreatureReady(player1, new LightshellDuo());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lightshell)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lightshell)).isEqualTo(4);
    }

    @Test
    void surveilCanKeepBothCardsInReverseOrder() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        castLightshellAndResolveEnterTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void surveilCanPutBothCardsIntoGraveyard() {
        Card first = new GrizzlyBears();
        Card second = new Shock();
        Card third = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second, third));
        castLightshellAndResolveEnterTrigger();

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
    }

    @Test
    void surveilWithOneCardLooksAtOnlyThatCard() {
        Card onlyCard = new Shock();
        harness.setLibrary(player1, List.of(onlyCard));
        castLightshellAndResolveEnterTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    void surveilWithEmptyLibraryFinishesWithoutInput() {
        harness.setLibrary(player1, List.of());
        castLightshellAndResolveEnterTrigger();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Lightshell Duo");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void creatureSpellDoesNotTriggerProwess() {
        Permanent lightshell = addCreatureReady(player1, new LightshellDuo());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lightshell)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lightshell)).isEqualTo(4);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void opponentsNoncreatureSpellDoesNotTriggerProwess() {
        Permanent lightshell = addCreatureReady(player1, new LightshellDuo());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lightshell)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, lightshell)).isEqualTo(4);
        harness.assertLife(player1, 18);
    }

    @Test
    void prowessResolvesBeforeSpellAndStacksForEachCast() {
        Permanent lightshell = addCreatureReady(player1, new LightshellDuo());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, lightshell)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, lightshell)).isEqualTo(5);
        harness.assertLife(player2, 20);
        resolveAllTriggers();

        harness.castInstant(player1, 0, player2.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, lightshell)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, lightshell)).isEqualTo(6);
        harness.assertLife(player2, 16);
    }

    private void castLightshellAndResolveEnterTrigger() {
        harness.setHand(player1, List.of(new LightshellDuo()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }
}
