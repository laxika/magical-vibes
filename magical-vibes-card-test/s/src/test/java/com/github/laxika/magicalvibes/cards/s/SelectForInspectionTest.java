package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DukharaPeafowl;
import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SelectForInspection.class, DukharaPeafowl.class, PropheticPrism.class})
class SelectForInspectionTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a tapped creature to its owner's hand and offers scry 1")
    void returnsTappedCreatureAndScries() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DukharaPeafowl());
        target.tap();

        harness.setHand(player1, List.of(new SelectForInspection()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Dukhara Peafowl");
        harness.assertInHand(player2, "Dukhara Peafowl");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(1);
    }

    @Test
    @DisplayName("Cannot target an untapped creature")
    void cannotTargetUntappedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DukharaPeafowl());

        harness.setHand(player1, List.of(new SelectForInspection()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a tapped creature");
    }

    @Test
    void cannotTargetTappedNoncreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new PropheticPrism());
        target.tap();
        harness.setHand(player1, List.of(new SelectForInspection()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void untappedTargetStopsBothBounceAndScry() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DukharaPeafowl());
        target.tap();
        SelectForInspection topCard = new SelectForInspection();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new SelectForInspection()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());
        target.untap();

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Dukhara Peafowl");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        harness.assertInGraveyard(player1, "Select for Inspection");
    }

    @Test
    void canReturnOwnCreatureAndKeepScryCardOnTop() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new DukharaPeafowl());
        target.tap();
        SelectForInspection topCard = new SelectForInspection();
        DukharaPeafowl nextCard = new DukharaPeafowl();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new SelectForInspection()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player1, "Dukhara Peafowl");
        harness.assertNotOnBattlefield(player1, "Dukhara Peafowl");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(topCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard, nextCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void canPutScryCardOnBottom() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DukharaPeafowl());
        target.tap();
        SelectForInspection topCard = new SelectForInspection();
        DukharaPeafowl nextCard = new DukharaPeafowl();
        harness.setLibrary(player1, List.of(topCard, nextCard));
        harness.setHand(player1, List.of(new SelectForInspection()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nextCard, topCard);
        harness.assertInHand(player2, "Dukhara Peafowl");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
    }

    @Test
    void returnsCreatureWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DukharaPeafowl());
        target.tap();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new SelectForInspection()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player2, "Dukhara Peafowl");
        harness.assertNotOnBattlefield(player2, "Dukhara Peafowl");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
