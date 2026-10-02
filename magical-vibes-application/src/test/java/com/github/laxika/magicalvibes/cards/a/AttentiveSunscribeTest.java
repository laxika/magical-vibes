package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.l.LodestoneNeedle;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AttentiveSunscribe.class, ArmoredKincaller.class, LodestoneNeedle.class})
class AttentiveSunscribeTest extends BaseCardTest {

    @Test
    @DisplayName("Attentive Sunscribe scries 1 when it becomes tapped")
    void becomingTappedTriggersScry() {
        Permanent sunscribe = addCreatureReady(player1, new AttentiveSunscribe());
        harness.setLibrary(player1, List.of(new ArmoredKincaller(), new ArmoredKincaller()));
        Card originalTop = gd.playerDecks.get(player1.getId()).getFirst();

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(originalTop);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId()).getLast()).isSameAs(originalTop);
        assertThat(sunscribe.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attentive Sunscribe does not trigger when another creature becomes tapped")
    void anotherCreatureBecomingTappedDoesNotTriggerScry() {
        harness.addToBattlefield(player1, new AttentiveSunscribe());
        addCreatureReady(player1, new ArmoredKincaller());

        declareAttackers(List.of(1));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An opponent's tap effect makes the Sunscribe's controller scry")
    void opponentTappingSunscribeTriggersItsControllersScry() {
        Permanent sunscribe = harness.addToBattlefieldAndReturn(player1, new AttentiveSunscribe());
        Card top = new AttentiveSunscribe();
        harness.setLibrary(player1, List.of(top));
        harness.setLibrary(player2, List.of(new AttentiveSunscribe()));
        harness.setHand(player2, List.of(new LodestoneNeedle()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castArtifact(player2, 0, sunscribe.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(sunscribe.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).containsExactly(top);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(top);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Tapping an already tapped Sunscribe does not trigger scry")
    void tappingAlreadyTappedSunscribeDoesNotTrigger() {
        Permanent sunscribe = harness.addToBattlefieldAndReturn(player1, new AttentiveSunscribe());
        sunscribe.tap();
        harness.setLibrary(player1, List.of(new AttentiveSunscribe()));
        harness.setHand(player1, List.of(new LodestoneNeedle()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0, sunscribe.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(sunscribe.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Each Sunscribe triggers only for its own tap")
    void twoSunscribesEachScryOnceWhenAttacking() {
        addCreatureReady(player1, new AttentiveSunscribe());
        addCreatureReady(player1, new AttentiveSunscribe());
        Card first = new AttentiveSunscribe();
        Card second = new AttentiveSunscribe();
        harness.setLibrary(player1, List.of(first, second));

        declareAttackers(List.of(0, 1));
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        PendingInteraction.Scry firstScry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(firstScry).isNotNull();
        assertThat(firstScry.cards()).containsExactly(first);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();

        PendingInteraction.Scry secondScry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(secondScry).isNotNull();
        assertThat(secondScry.cards()).containsExactly(second);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scry resolves with an empty library without requiring a choice")
    void emptyLibraryScryResolvesWithoutChoice() {
        addCreatureReady(player1, new AttentiveSunscribe());
        harness.setLibrary(player1, List.of());

        declareAttackers(List.of(0));
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
