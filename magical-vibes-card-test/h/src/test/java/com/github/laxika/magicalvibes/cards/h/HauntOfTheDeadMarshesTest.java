package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.ArvadTheCursed;
import com.github.laxika.magicalvibes.cards.m.MinasTirith;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HauntOfTheDeadMarshes.class, ArvadTheCursed.class, MinasTirith.class})
class HauntOfTheDeadMarshesTest extends BaseCardTest {

    @Test
    void enteringOffersScryOne() {
        harness.setHand(player1, List.of(new HauntOfTheDeadMarshes()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.Scry scry = gd.interaction.activeInteraction(PendingInteraction.Scry.class);
        assertThat(scry).isNotNull();
        assertThat(scry.cards()).hasSize(1);

        Card topCard = scry.cards().getFirst();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0), List.of()));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst()).isSameAs(topCard);
    }

    @Test
    void graveyardAbilityReturnsItTappedWithLegendaryCreature() {
        harness.addToBattlefield(player1, new ArvadTheCursed());
        harness.setGraveyard(player1, List.of(new HauntOfTheDeadMarshes()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        forceMainPhase();

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent haunt = findPermanent(player1, "Haunt of the Dead Marshes");
        assertThat(haunt.isTapped()).isTrue();
        harness.assertNotInGraveyard(player1, "Haunt of the Dead Marshes");
    }

    @Test
    void cannotActivateWithoutLegendaryCreature() {
        harness.setGraveyard(player1, List.of(new HauntOfTheDeadMarshes()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        forceMainPhase();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Haunt of the Dead Marshes");
    }

    @Test
    void cannotActivateWithOnlyLegendaryNoncreature() {
        harness.addToBattlefield(player1, new MinasTirith());
        harness.setGraveyard(player1, List.of(new HauntOfTheDeadMarshes()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        forceMainPhase();

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "Haunt of the Dead Marshes");
    }

    private void forceMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }
}
