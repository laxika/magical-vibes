package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GoldmeadowDodger;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HoofprintsOfTheStag.class, GoldmeadowDodger.class})
class HoofprintsOfTheStagTest extends BaseCardTest {

    // ===== Draw trigger: "you may put a hoofprint counter on this enchantment" =====

    @Test
    @DisplayName("Accepting the draw trigger puts a hoofprint counter on the enchantment")
    void drawAddsCounterWhenAccepted() {
        Permanent hoofprints = harness.addToBattlefieldAndReturn(player1, new HoofprintsOfTheStag());

        drawAndSurfaceMay(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hoofprints.getCounterCount(CounterType.HOOFPRINT)).isEqualTo(1);
    }

    @Test
    @DisplayName("Each accepted draw trigger adds its own hoofprint counter")
    void eachDrawAddsItsOwnCounter() {
        Permanent hoofprints = harness.addToBattlefieldAndReturn(player1, new HoofprintsOfTheStag());

        drawAndSurfaceMay(player1);
        harness.handleMayAbilityChosen(player1, true);
        drawAndSurfaceMay(player1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(hoofprints.getCounterCount(CounterType.HOOFPRINT)).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the draw trigger adds no counter")
    void drawAddsNoCounterWhenDeclined() {
        Permanent hoofprints = harness.addToBattlefieldAndReturn(player1, new HoofprintsOfTheStag());

        drawAndSurfaceMay(player1);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(hoofprints.getCounterCount(CounterType.HOOFPRINT)).isZero();
    }

    @Test
    @DisplayName("Opponent drawing does not trigger the enchantment")
    void opponentDrawDoesNotTrigger() {
        Permanent hoofprints = harness.addToBattlefieldAndReturn(player1, new HoofprintsOfTheStag());

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player2.getId()));

        assertThat(gd.stack).isEmpty(); // player1's ON_CONTROLLER_DRAWS does not fire on the opponent's draw
        assertThat(hoofprints.getCounterCount(CounterType.HOOFPRINT)).isZero();
    }

    // ===== Activated ability: create a 4/4 white Elemental with flying =====

    @Test
    @DisplayName("Removing four hoofprint counters creates a 4/4 white Elemental with flying")
    void abilityCreatesFlyingElemental() {
        Permanent hoofprints = harness.addToBattlefieldAndReturn(player1, new HoofprintsOfTheStag());
        hoofprints.setCounterCount(CounterType.HOOFPRINT, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(hoofprints);
        harness.activateAbility(player1, idx, null, null);
        harness.passBothPriorities(); // resolve token creation

        Permanent token = findPermanent(player1, "Elemental");
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, token, Keyword.FLYING)).isTrue();
        assertThat(hoofprints.getCounterCount(CounterType.HOOFPRINT)).isZero();
    }

    @Test
    @DisplayName("Cannot activate with fewer than four hoofprint counters")
    void cannotActivateWithoutFourCounters() {
        Permanent hoofprints = harness.addToBattlefieldAndReturn(player1, new HoofprintsOfTheStag());
        hoofprints.setCounterCount(CounterType.HOOFPRINT, 3);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(hoofprints);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without the full mana cost")
    void cannotActivateWithoutEnoughMana() {
        Permanent hoofprints = harness.addToBattlefieldAndReturn(player1, new HoofprintsOfTheStag());
        hoofprints.setCounterCount(CounterType.HOOFPRINT, 4);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.WHITE, 2);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(hoofprints);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
        assertThat(hoofprints.getCounterCount(CounterType.HOOFPRINT)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot activate during the opponent's turn")
    void cannotActivateOnOpponentsTurn() {
        Permanent hoofprints = harness.addToBattlefieldAndReturn(player1, new HoofprintsOfTheStag());
        hoofprints.setCounterCount(CounterType.HOOFPRINT, 4);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.WHITE, 3);

        int idx = gd.playerBattlefields.get(player1.getId()).indexOf(hoofprints);
        assertThatThrownBy(() -> harness.activateAbility(player1, idx, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    // The controller draws a card — ON_CONTROLLER_DRAWS puts the "you may" trigger on the stack
    // (CR 603.5); resolving it surfaces the MayAbilityChoice so a choice can be made.
    private void drawAndSurfaceMay(Player player) {
        harness.setLibrary(player, List.of(new GoldmeadowDodger())); // ensure a card to draw
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
