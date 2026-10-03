package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
import com.github.laxika.magicalvibes.cards.m.MillennialGargoyle;
import com.github.laxika.magicalvibes.cards.m.Mugging;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CartelAristocrat.class, MillennialGargoyle.class, ArmoredTransport.class, Mugging.class})
class CartelAristocratTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing another creature grants protection from the chosen color")
    void grantsProtectionFromChosenColor() {
        Permanent aristocrat = addCreatureReady(player1, new CartelAristocrat());
        harness.addToBattlefield(player1, new MillennialGargoyle());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "RED");

        harness.assertInGraveyard(player1, "Millennial Gargoyle");
        assertThat(aristocrat.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.RED);
    }

    @Test
    @DisplayName("Protection wears off at end of turn")
    void protectionClearedAtEndOfTurn() {
        Permanent aristocrat = addCreatureReady(player1, new CartelAristocrat());
        harness.addToBattlefield(player1, new MillennialGargoyle());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");
        assertThat(aristocrat.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.WHITE);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(aristocrat.getProtectionFromColorsUntilEndOfTurn()).doesNotContain(CardColor.WHITE);
    }

    @Test
    @DisplayName("Cannot activate when Cartel Aristocrat is the only creature")
    void cannotSacrificeItself() {
        addCreatureReady(player1, new CartelAristocrat());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Cartel Aristocrat");
    }

    @Test
    @DisplayName("Choosing Cartel Aristocrat itself as the sacrifice is rejected")
    void choosingItselfIsRejected() {
        Permanent aristocrat = addCreatureReady(player1, new CartelAristocrat());
        harness.addToBattlefield(player1, new MillennialGargoyle());
        harness.addToBattlefield(player1, new ArmoredTransport());

        harness.activateAbility(player1, 0, null, null);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, aristocrat.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Cartel Aristocrat");
        harness.assertOnBattlefield(player1, "Millennial Gargoyle");
        harness.assertOnBattlefield(player1, "Armored Transport");
    }

    @Test
    @DisplayName("The chosen creature is the one sacrificed")
    void chosenCreatureIsSacrificed() {
        Permanent aristocrat = addCreatureReady(player1, new CartelAristocrat());
        harness.addToBattlefield(player1, new MillennialGargoyle());
        harness.addToBattlefield(player1, new ArmoredTransport());
        UUID transportId = harness.getPermanentId(player1, "Armored Transport");

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, transportId);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        harness.assertInGraveyard(player1, "Armored Transport");
        harness.assertOnBattlefield(player1, "Millennial Gargoyle");
        assertThat(aristocrat.getProtectionFromColorsUntilEndOfTurn()).contains(CardColor.BLACK);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately, before protection resolves")
    void sacrificeIsPaidBeforeResolution() {
        Permanent aristocrat = addCreatureReady(player1, new CartelAristocrat());
        harness.addToBattlefield(player1, new MillennialGargoyle());

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Millennial Gargoyle");
        assertThat(aristocrat.getProtectionFromColorsUntilEndOfTurn()).isEmpty();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");
        assertThat(aristocrat.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.BLUE);
    }

    @Test
    @DisplayName("The ability can be activated while tapped and summoning sick")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent aristocrat = harness.addToBattlefieldAndReturn(player1, new CartelAristocrat());
        aristocrat.setSummoningSick(true);
        aristocrat.setTapped(true);
        harness.addToBattlefield(player1, new MillennialGargoyle());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        harness.assertInGraveyard(player1, "Millennial Gargoyle");
        assertThat(aristocrat.getProtectionFromColorsUntilEndOfTurn()).containsExactly(CardColor.GREEN);
    }

    @Test
    @DisplayName("Repeated activations retain protection from both chosen colors")
    void repeatedActivationsAccumulateProtection() {
        Permanent aristocrat = addCreatureReady(player1, new CartelAristocrat());
        harness.addToBattlefield(player1, new MillennialGargoyle());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "WHITE");

        harness.addToBattlefield(player1, new ArmoredTransport());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLACK");

        harness.assertInGraveyard(player1, "Millennial Gargoyle");
        harness.assertInGraveyard(player1, "Armored Transport");
        assertThat(aristocrat.getProtectionFromColorsUntilEndOfTurn())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLACK);
    }

    @Test
    @DisplayName("An opponent's creature cannot pay the sacrifice cost")
    void cannotPayWithOpponentsCreature() {
        addCreatureReady(player1, new CartelAristocrat());
        harness.addToBattlefield(player2, new MillennialGargoyle());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Cartel Aristocrat");
        harness.assertOnBattlefield(player2, "Millennial Gargoyle");
    }

    @Test
    @DisplayName("Protection gained in response makes a red spell's target illegal")
    void protectionInResponseInvalidatesRedSpellTarget() {
        Permanent aristocrat = addCreatureReady(player2, new CartelAristocrat());
        harness.addToBattlefield(player2, new MillennialGargoyle());
        harness.setHand(player1, List.of(new Mugging()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, aristocrat.getId());
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player2, "RED");
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Cartel Aristocrat");
        harness.assertInGraveyard(player2, "Millennial Gargoyle");
        harness.assertInGraveyard(player1, "Mugging");
        assertThat(aristocrat.getMarkedDamage()).isZero();
        assertThat(aristocrat.isCantBlockThisTurn()).isFalse();
    }
}
