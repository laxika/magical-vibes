package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.v.VernadiShieldmate;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DazzlingLights.class, VernadiShieldmate.class, DimirGuildgate.class})
class DazzlingLightsTest extends BaseCardTest {

    @Test
    @DisplayName("Gives target creature -3/-0 and surveils two")
    void weakensTargetAndSurveilsTwo() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        Card topCard = new VernadiShieldmate();
        Card secondCard = new DimirGuildgate();
        harness.setLibrary(player1, List.of(topCard, secondCard));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());

        assertThat(bears.getPowerModifier()).isEqualTo(-3);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
        PendingInteraction.Scry surveil = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(surveil).isNotNull();
        assertThat(surveil.cards()).containsExactly(topCard, secondCard);

        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondCard);
    }

    @Test
    @DisplayName("The -3/-0 wears off at cleanup")
    void debuffWearsOffAtCleanup() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        harness.setLibrary(player1, List.of(new VernadiShieldmate(), new DimirGuildgate()));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, bears.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(bears.getPowerModifier()).isEqualTo(0);
        assertThat(bears.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new VernadiShieldmate());
        Permanent island = harness.addToBattlefieldAndReturn(player2, new DimirGuildgate());
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, island.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Can keep both surveilled cards in reverse order")
    void keepsBothCardsInChosenOrder() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new VernadiShieldmate());
        Card first = new VernadiShieldmate();
        Card second = new DimirGuildgate();
        Card third = new DazzlingLights();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1, 0), List.of()));

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(first, second);
    }

    @Test
    @DisplayName("Can put both surveilled cards into the graveyard")
    void putsBothCardsIntoGraveyard() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        Card first = new VernadiShieldmate();
        Card second = new DimirGuildgate();
        Card third = new DazzlingLights();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
    }

    @Test
    @DisplayName("Surveils the only card when the library contains fewer than two cards")
    void surveilsOneCardFromShortLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        Card onlyCard = new DimirGuildgate();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new DazzlingLights()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(onlyCard);
        assertThat(target.getPowerModifier()).isEqualTo(-3);
    }

    @Test
    @DisplayName("Still weakens the target when the library is empty")
    void resolvesWithEmptyLibrary() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        Card spell = new DazzlingLights();
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(target.getPowerModifier()).isEqualTo(-3);
        assertThat(target.getToughnessModifier()).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
    }

    @Test
    @DisplayName("Does not surveil when its only target leaves the battlefield")
    void doesNotSurveilWithIllegalTarget() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new VernadiShieldmate());
        Card first = new VernadiShieldmate();
        Card second = new DimirGuildgate();
        Card spell = new DazzlingLights();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerHands.get(player2.getId()).add(target.getCard());
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell).doesNotContain(first, second);
    }
}
