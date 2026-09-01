package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SanguineSpy.class, Forest.class, GrizzlyBears.class, HillGiant.class, Island.class, Murder.class, Shock.class})
class SanguineSpyTest extends BaseCardTest {

    @Test
    @DisplayName("Pays {1} and sacrifices another creature to surveil 1")
    void sacrificesAnotherCreatureAndSurveils() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Card topCard = new Island();
        harness.setLibrary(player1, List.of(topCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Sanguine Spy");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(topCard);
    }

    @Test
    @DisplayName("Cannot sacrifice Sanguine Spy for its own ability")
    void cannotSacrificeItself() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("At the end step, five distinct graveyard mana values may be paid for a draw")
    void endStepMayPayLifeToDrawWithFiveDistinctManaValues() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.setGraveyard(player1, graveyardWithFiveDistinctManaValues());
        Card drawnCard = new Island();
        harness.setLibrary(player1, List.of(drawnCard));

        advanceToEndStep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        int lifeBefore = gd.getLife(player1.getId());
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }

    @Test
    @DisplayName("The end-step draw does not trigger with fewer than five distinct graveyard mana values")
    void endStepDoesNotTriggerBelowThreshold() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.setGraveyard(player1, graveyardWithFiveDistinctManaValues().subList(0, 4));

        advanceToEndStep(player1);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The end-step draw triggers only during its controller's end step")
    void endStepTriggerDoesNotFireOnOpponentsEndStep() {
        harness.addToBattlefield(player1, new SanguineSpy());
        harness.setGraveyard(player1, graveyardWithFiveDistinctManaValues());

        advanceToEndStep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private List<Card> graveyardWithFiveDistinctManaValues() {
        return List.of(new Forest(), new Shock(),
                new GrizzlyBears(), new Murder(), new HillGiant());
    }

    private void advanceToEndStep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
