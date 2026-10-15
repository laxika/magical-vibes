package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StarbreachWhale.class, GrizzlyBears.class})
class StarbreachWhaleTest extends BaseCardTest {

    @Test
    @DisplayName("When Starbreach Whale enters, its controller surveils 2")
    void entersWithSurveilTwo() {
        Card first = new GrizzlyBears();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));
        harness.setHand(player1, List.of(new StarbreachWhale()));
        addNormalMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(first, second);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(1), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("Can be cast using its warp cost")
    void canBeCastForWarpCost() {
        harness.setHand(player1, List.of(new StarbreachWhale()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Starbreach Whale");
    }

    private void addNormalMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    void surveilCanKeepBothInReverseOrderOrPutBothInGraveyard(boolean putBothInGraveyard) {
        Card first = new StarbreachWhale();
        Card second = new StarbreachWhale();
        Card third = new StarbreachWhale();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new StarbreachWhale()));
        addNormalMana();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.ScryOrder(
                putBothInGraveyard ? List.of() : List.of(1, 0),
                putBothInGraveyard ? List.of(0, 1) : List.of()));

        if (putBothInGraveyard) {
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third);
            assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(first, second);
        } else {
            assertThat(gd.playerDecks.get(player1.getId())).containsExactly(second, first, third);
            assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        }
    }

    @Test
    void surveilWithOneCardCanPutItInGraveyard() {
        Card onlyCard = new StarbreachWhale();
        harness.setLibrary(player1, List.of(onlyCard));
        harness.setHand(player1, List.of(new StarbreachWhale()));
        addNormalMana();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards())
                .containsExactly(onlyCard);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0)));

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(onlyCard);
    }

    @Test
    void normalCastWithEmptyLibraryStaysThroughEndStep() {
        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new StarbreachWhale()));
        addNormalMana();
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Starbreach Whale");
    }

    @Test
    void warpExilesThenAllowsNormalCastOnLaterTurnWithAnotherSurveil() {
        StarbreachWhale whale = new StarbreachWhale();
        harness.setLibrary(player1, List.of(new StarbreachWhale(), new StarbreachWhale(),
                new StarbreachWhale(), new StarbreachWhale()));
        harness.setLibrary(player2, List.of(new StarbreachWhale(), new StarbreachWhale()));
        harness.setHand(player1, List.of(whale));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        resolveAllTriggers();
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(0, 1), List.of()));

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertNotOnBattlefield(player1, "Starbreach Whale");
        assertThat(gd.findExiledCard(whale.getId())).isNotNull();

        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);
        addNormalMana();
        harness.castFromExile(player1, whale.getId());
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.ScryOrder(List.of(), List.of(0, 1)));
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.findExiledCard(whale.getId())).isNull();

        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Starbreach Whale");
    }
}
