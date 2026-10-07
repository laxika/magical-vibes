package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.n.NessianCourser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StymiedHopes.class, NessianCourser.class})
class StymiedHopesTest extends BaseCardTest {

    @Test
    @DisplayName("Counters a spell when its controller does not pay and then scries 1")
    void countersSpellAndScries() {
        NessianCourser bears = new NessianCourser();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.setHand(player2, List.of(new StymiedHopes()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        harness.assertInGraveyard(player1, "Nessian Courser");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        harness.assertInGraveyard(player2, "Stymied Hopes");
    }

    @Test
    @DisplayName("Lets the spell resolve when its controller pays {1} and then scries 1")
    void payingOneLetsSpellResolveAndScries() {
        NessianCourser bears = new NessianCourser();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new StymiedHopes()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, bears.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nessian Courser");
        harness.assertInGraveyard(player2, "Stymied Hopes");
    }

    @Test
    @DisplayName("Declining an affordable payment counters the spell and permits keeping the scryed card")
    void decliningPaymentCountersAndKeepsTopCard() {
        NessianCourser creature = new NessianCourser();
        NessianCourser top = new NessianCourser();
        StymiedHopes bottom = new StymiedHopes();
        harness.setLibrary(player2, List.of(top, bottom));
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new StymiedHopes()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castAndResolveInstant(player2, 0, creature.getId());
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Nessian Courser");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.ScryOrder(List.of(0), List.of()));
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top, bottom);
        harness.assertInGraveyard(player2, "Stymied Hopes");
    }

    @Test
    @DisplayName("Can counter a noncreature spell; its victim does not scry after being countered")
    void countersAnotherStymiedHopes() {
        NessianCourser creature = new NessianCourser();
        StymiedHopes firstCounter = new StymiedHopes();
        NessianCourser top = new NessianCourser();
        StymiedHopes bottom = new StymiedHopes();
        harness.setLibrary(player1, List.of(top, bottom));
        harness.setHand(player1, List.of(creature, new StymiedHopes()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(firstCounter));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, firstCounter.getId());

        harness.assertInGraveyard(player2, "Stymied Hopes");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(bottom, top);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Nessian Courser");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    @DisplayName("Does not scry when its only target leaves the stack before resolution")
    void missingTargetPreventsScry() {
        NessianCourser creature = new NessianCourser();
        NessianCourser top = new NessianCourser();
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(top));
        harness.setHand(player1, List.of(creature, new StymiedHopes()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.setHand(player2, List.of(new StymiedHopes()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, creature.getId());
        harness.passPriority(player2);
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.assertInGraveyard(player1, "Nessian Courser");
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Stymied Hopes");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(top);
    }
}
