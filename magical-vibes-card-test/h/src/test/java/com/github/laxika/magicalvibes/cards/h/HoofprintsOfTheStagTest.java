package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FlickerOfFate;
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

@CardUsed({HoofprintsOfTheStag.class, GoldmeadowDodger.class, FlickerOfFate.class})
class HoofprintsOfTheStagTest extends BaseCardTest {


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

    @Test
    @DisplayName("A draw trigger cannot put a counter on the enchantment after it leaves and returns")
    void oldDrawTriggerDoesNotAffectReturnedEnchantment() {
        Permanent hoofprints = harness.addToBattlefieldAndReturn(player1, new HoofprintsOfTheStag());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new GoldmeadowDodger()));
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        harness.setHand(player1, List.of(new FlickerOfFate()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.castInstant(player1, 0, hoofprints.getId());
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Hoofprints of the Stag");
        assertThat(returned.getId()).isNotEqualTo(hoofprints.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(returned.getCounterCount(CounterType.HOOFPRINT)).isZero();
    }

    @Test
    @DisplayName("Each copy receives only the counter from its own draw trigger")
    void twoCopiesReceiveIndependentCounters() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new HoofprintsOfTheStag());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new HoofprintsOfTheStag());

        drawAndSurfaceMay(player1);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(first.getCounterCount(CounterType.HOOFPRINT)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.HOOFPRINT)).isEqualTo(1);
    }

    @Test
    @DisplayName("Activation during upkeep pays counters immediately and resolves even if the source leaves")
    void upkeepActivationPaysCountersBeforeResolvingAndSurvivesSourceRemoval() {
        Permanent hoofprints = harness.addToBattlefieldAndReturn(player1, new HoofprintsOfTheStag());
        hoofprints.setCounterCount(CounterType.HOOFPRINT, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(hoofprints.getCounterCount(CounterType.HOOFPRINT)).isEqualTo(1);
        assertThat(countPermanents(player1, "Elemental")).isZero();
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, hoofprints));
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Elemental")).isEqualTo(1);
        assertThat(countPermanents(player2, "Elemental")).isZero();
    }

    private void drawAndSurfaceMay(Player player) {
        harness.setLibrary(player, List.of(new GoldmeadowDodger())); // ensure a card to draw
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        resolveAllTriggers();
    }
}
