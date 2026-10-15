package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CrustaceanCommando;
import com.github.laxika.magicalvibes.cards.e.EPFPointSquad;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.Terminate;
import com.github.laxika.magicalvibes.cards.z.ZooEscapees;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MondoGecko.class, CrustaceanCommando.class, ZooEscapees.class, EPFPointSquad.class, Island.class,
        Terminate.class})
class MondoGeckoTest extends BaseCardTest {

    @Test
    @DisplayName("Discarding a card makes Mondo Gecko the chosen color and grants hexproof from it")
    void discardAbilityUsesOneColorForBothEffects() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mondo = harness.addToBattlefieldAndReturn(player1, new MondoGecko());
        harness.setHand(player1, new ArrayList<>(List.of(new ZooEscapees())));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mondo), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class)).isNotNull();
        harness.handleListChoice(player1, "RED");

        assertThat(gqs.getEffectiveColors(gd, mondo)).containsExactly(CardColor.RED);
        assertThat(gqs.hasHexproofFromColor(gd, mondo, CardColor.RED)).isTrue();
    }

    @Test
    @DisplayName("Chosen color and hexproof wear off at end of turn")
    void discardAbilityExpiresAtEndOfTurn() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mondo = harness.addToBattlefieldAndReturn(player1, new MondoGecko());
        harness.setHand(player1, new ArrayList<>(List.of(new ZooEscapees())));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mondo), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        gd.interaction.clearAwaitingInput();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveColors(gd, mondo)).containsExactly(CardColor.BLUE);
        assertThat(gqs.hasHexproofFromColor(gd, mondo, CardColor.RED)).isFalse();
    }

    @Test
    @DisplayName("Combat damage draws once for each distinct color among controlled permanents")
    void combatDamageDrawsForEachDistinctControlledColor() {
        Permanent mondo = addCreatureReady(player1, new MondoGecko());
        mondo.setAttacking(true);
        harness.addToBattlefield(player1, new ZooEscapees());
        harness.addToBattlefield(player1, new ZooEscapees());
        harness.addToBattlefield(player1, new EPFPointSquad());
        harness.addToBattlefield(player1, new CrustaceanCommando());
        harness.setLibrary(player1, List.of(new ZooEscapees(), new ZooEscapees(),
                new ZooEscapees(), new ZooEscapees()));

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 4);
    }

    @Test
    @DisplayName("Repeated activations replace the color but retain hexproof from both chosen colors")
    void repeatedActivationsAccumulateHexproof() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mondo = harness.addToBattlefieldAndReturn(player1, new MondoGecko());
        harness.setHand(player1, List.of(new Island(), new ZooEscapees()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        for (String color : List.of("RED", "GREEN")) {
            harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mondo), null, null);
            harness.handleCardChosen(player1, 0);
            harness.passBothPriorities();
            harness.handleListChoice(player1, color);
        }

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gqs.getEffectiveColors(gd, mondo)).containsExactly(CardColor.GREEN);
        assertThat(gqs.hasHexproofFromColor(gd, mondo, CardColor.RED)).isTrue();
        assertThat(gqs.hasHexproofFromColor(gd, mondo, CardColor.GREEN)).isTrue();
        assertThat(gqs.hasHexproofFromColor(gd, mondo, CardColor.BLUE)).isFalse();
    }

    @Test
    @DisplayName("Multicolored permanents contribute each color, but colorless lands and opposing permanents do not")
    void combatDamageCountsMulticolorButNotColorlessOrOpposingPermanents() {
        Permanent mondo = addCreatureReady(player1, new MondoGecko());
        mondo.setAttacking(true);
        harness.addToBattlefield(player1, new EPFPointSquad());
        harness.addToBattlefield(player1, new EPFPointSquad());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new ZooEscapees());
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
    }

    @Test
    @DisplayName("The draw trigger counts current colors when it resolves")
    void combatDamageCountsColorsAtResolution() {
        Permanent mondo = addCreatureReady(player1, new MondoGecko());
        mondo.setAttacking(true);
        harness.addToBattlefield(player1, new EPFPointSquad());
        harness.setHand(player1, List.of(new Island()));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));

        resolveCombat();
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mondo), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Hexproof from red invalidates an opposing black-red spell already on the stack")
    void hexproofStopsMulticoloredSpellWithMatchingNonprimaryColor() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        Permanent mondo = harness.addToBattlefieldAndReturn(player1, new MondoGecko());
        harness.setHand(player1, List.of(new Island()));
        harness.setHand(player2, List.of(new Terminate()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, mondo.getId());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mondo), null, null);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Mondo Gecko");
        harness.assertNotInGraveyard(player1, "Mondo Gecko");
        harness.assertInGraveyard(player2, "Terminate");
    }
}
