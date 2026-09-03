package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GiantMantis;
import com.github.laxika.magicalvibes.cards.s.Stupor;
import com.github.laxika.magicalvibes.cards.t.TaintedSpecter;
import com.github.laxika.magicalvibes.cards.u.UnfulfilledDesires;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MangarasBlessing.class, Stupor.class, TaintedSpecter.class, UnfulfilledDesires.class,
        GiantMantis.class})
class MangarasBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Casting it gains the controller 5 life")
    void castingGains5Life() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new MangarasBlessing()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
        harness.assertInGraveyard(player1, "Mangara's Blessing");
    }

    @Test
    @DisplayName("Discarded by an opponent's spell: gains 2 life without prompting for a target")
    void discardedByOpponentGains2Life() {
        harness.setHand(player2, new ArrayList<>(List.of(new MangarasBlessing())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Stupor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        // The trigger is non-targeting — it goes straight on the stack, no permanent choice
        assertThat(gd.interaction.activeInteraction()).isNull();

        harness.passBothPriorities(); // resolve the discard trigger

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
        harness.assertInGraveyard(player2, "Mangara's Blessing");
    }

    @Test
    @DisplayName("Returns from the graveyard to hand at the beginning of the next end step")
    void returnsToHandAtNextEndStep() {
        harness.setHand(player2, new ArrayList<>(List.of(new MangarasBlessing())));

        harness.setHand(player1, List.of(new Stupor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());
        harness.passBothPriorities(); // resolve the discard trigger

        harness.assertInGraveyard(player2, "Mangara's Blessing");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd); // -> END_STEP

        harness.assertNotInGraveyard(player2, "Mangara's Blessing");
        assertThat(gd.playerHands.get(player2.getId()))
                .extracting(c -> c.getName())
                .contains("Mangara's Blessing");
    }

    @Test
    @DisplayName("Discarded by an opponent's activated ability: gains 2 life")
    void discardedByOpponentAbilityGains2Life() {
        harness.setLife(player2, 20);
        harness.setHand(player2, List.of(new MangarasBlessing()));

        harness.addToBattlefield(player1, new TaintedSpecter());
        findPermanent(player1, "Tainted Specter").setSummoningSick(false);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player2, false);
        harness.handleCardChosen(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(21);
        harness.assertInGraveyard(player2, "Mangara's Blessing");
    }

    @Test
    @DisplayName("Does not trigger when its own controller discards it")
    void doesNotTriggerOnSelfDiscard() {
        harness.setLife(player1, 20);

        gd.playerDecks.get(player1.getId()).add(new GiantMantis());
        gd.playerDecks.get(player1.getId()).add(new GiantMantis());
        gd.playerDecks.get(player1.getId()).add(new GiantMantis());

        harness.addToBattlefield(player1, new UnfulfilledDesires());
        harness.setHand(player1, List.of(new MangarasBlessing()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities(); // Unfulfilled Desires draws, then prompts for discard

        harness.handleCardChosen(player1, 0); // discard Mangara's Blessing

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        harness.assertInGraveyard(player1, "Mangara's Blessing");

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd); // -> END_STEP

        // No delayed return was registered
        harness.assertInGraveyard(player1, "Mangara's Blessing");
    }
}
