package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CuriosityCrafter.class, GrizzlyBears.class})
class CuriosityCrafterTest extends BaseCardTest {

    @Test
    @DisplayName("A creature token dealing combat damage draws a card")
    void tokenCombatDamageDrawsCard() {
        addCuriosityCrafter();
        addReadyToken();
        seedLibrary(1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(1));
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("A nontoken creature dealing combat damage does not draw a card")
    void nontokenCombatDamageDoesNotDrawCard() {
        addCuriosityCrafter();
        addCreatureReady(player1, new GrizzlyBears());
        seedLibrary(1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(1));
        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
    }

    @Test
    @DisplayName("Each token dealing combat damage draws a separate card")
    void eachTokenTriggersSeparately() {
        addCuriosityCrafter();
        addReadyToken();
        addReadyToken();
        seedLibrary(2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        declareAttackers(List.of(1, 2));
        resolveCombat();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 2);
    }

    @Test
    @DisplayName("The controller has no maximum hand size")
    void controllerHasNoMaximumHandSize() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        addCuriosityCrafter();
        harness.setHand(player1, new ArrayList<>(List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears())));

        harness.getGameService().advanceStep(gd);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(9);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void addCuriosityCrafter() {
        addCreatureReady(player1, new CuriosityCrafter());
    }

    private Permanent addReadyToken() {
        Card tokenCard = new GrizzlyBears();
        tokenCard.setToken(true);
        return addCreatureReady(player1, tokenCard);
    }

    private void seedLibrary(int count) {
        gd.playerDecks.get(player1.getId()).clear();
        for (int i = 0; i < count; i++) {
            gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        }
    }
}
