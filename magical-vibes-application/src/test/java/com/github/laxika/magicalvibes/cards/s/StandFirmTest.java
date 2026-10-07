package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.Arachnoid;
import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StandFirm.class, Arachnoid.class, ConjurersBauble.class})
class StandFirmTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts the target creature and scries 2")
    void boostsAndScriesTwo() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        harness.setHand(player1, List.of(new StandFirm()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    @DisplayName("Scry 2 can put both cards on the bottom")
    void scryTwoPutsCardsOnBottom() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        harness.setHand(player1, List.of(new StandFirm()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card firstTop = deck.get(0);
        Card secondTop = deck.get(1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(deck.get(deck.size() - 2)).isSameAs(firstTop);
        assertThat(deck.get(deck.size() - 1)).isSameAs(secondTop);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        harness.setHand(player1, List.of(new StandFirm()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Can target an opponent's creature")
    void canTargetOpponentsCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new Arachnoid());
        harness.setHand(player1, List.of(new StandFirm()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, opponentCreature.getId());

        assertThat(opponentCreature.getPowerModifier()).isEqualTo(1);
        assertThat(opponentCreature.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class)).isNotNull();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        UUID artifactId = harness.addToBattlefieldAndReturn(player1, new ConjurersBauble()).getId();
        harness.setHand(player1, List.of(new StandFirm()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifactId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Scry can reverse the top two cards without moving the rest")
    void reversesTopTwoCards() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        Card first = new Arachnoid();
        Card second = new ConjurersBauble();
        Card third = new StandFirm();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new StandFirm()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        harness.assertInGraveyard(player1, "Stand Firm");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scry can split cards between the top and bottom")
    void splitsTopAndBottom() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        Card first = new Arachnoid();
        Card second = new ConjurersBauble();
        Card third = new StandFirm();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new StandFirm()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, third, first);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scry can reverse the order of cards put on the bottom")
    void reversesBottomOrder() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        Card first = new Arachnoid();
        Card second = new ConjurersBauble();
        Card third = new StandFirm();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new StandFirm()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, second, first);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Scry 2 with a one-card library looks at only that card")
    void scriesOneCardFromShortLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        Card onlyCard = new ConjurersBauble();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new StandFirm()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An empty library does not prevent the boost or open a scry prompt")
    void resolvesWithEmptyLibrary() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new StandFirm()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(creature.getPowerModifier()).isEqualTo(1);
        assertThat(creature.getToughnessModifier()).isEqualTo(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Stand Firm");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not scry when its only target leaves the battlefield")
    void doesNotScryWithIllegalTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new Arachnoid());
        Card first = new ConjurersBauble();
        Card second = new Arachnoid();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new StandFirm()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.setGraveyard(player1, List.of(creature.getCard()));
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        harness.assertInGraveyard(player1, "Stand Firm");
        assertThat(gd.stack).isEmpty();
    }
}
