package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhyrexianDelver.class, GrizzlyBears.class, HolyDay.class})
class PhyrexianDelverTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a target creature from your graveyard and loses life equal to its mana value")
    void returnsTargetCreatureAndLosesLife() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new PhyrexianDelver(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Only creature cards are offered as ETB targets")
    void onlyCreatureCardsAreOfferedAsTargets() {
        Card instant = new HolyDay();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(instant, creature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new PhyrexianDelver(), "{3}{B}{B}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
    }

    @Test
    @DisplayName("Only your graveyard is offered as an ETB target source")
    void onlyYourGraveyardIsOfferedAsATargetSource() {
        Card yourCreature = new GrizzlyBears();
        Card opponentsCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(yourCreature));
        harness.setGraveyard(player2, List.of(opponentsCreature));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new PhyrexianDelver(), "{3}{B}{B}");
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(yourCreature.getId());
    }

    @Test
    @DisplayName("Fizzles without life loss when the targeted card leaves the graveyard")
    void fizzlesWhenTargetLeavesGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new PhyrexianDelver(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creature.getId()));
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Enters without losing life when there are no creature cards to target")
    void entersWithNoLegalGraveyardTarget() {
        harness.setGraveyard(player1, List.of(new HolyDay()));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new PhyrexianDelver(), "{3}{B}{B}");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Phyrexian Delver");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The trigger resolves after its source leaves and uses the returned card's mana value")
    void resolvesAfterSourceLeavesBattlefield() {
        Card creature = new PhyrexianDelver();
        harness.setGraveyard(player1, List.of(creature));
        harness.setLife(player1, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castFromHand(player1, new PhyrexianDelver(), "{3}{B}{B}");
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        gd.playerBattlefields.get(player1.getId()).clear();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .singleElement().satisfies(p -> {
                    assertThat(p.getCard().getId()).isEqualTo(creature.getId());
                    assertThat(p.isTapped()).isFalse();
                });
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 15);
    }
}
