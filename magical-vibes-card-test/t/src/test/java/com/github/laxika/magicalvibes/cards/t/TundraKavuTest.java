package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TundraKavu.class, YavimayaCoast.class, Dodecapod.class})
class TundraKavuTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability offers only Plains or Island")
    void resolvingOffersOnlyPlainsOrIsland() {
        Permanent land = addKavuAndLand();

        activateAbility(land);

        PendingInteraction.ColorChoice choice = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(choice.options()).containsExactly("PLAINS", "ISLAND");
    }

    @Test
    @DisplayName("Choosing Plains replaces the target land's type")
    void choosingPlainsReplacesLandType() {
        Permanent land = addKavuAndLand();

        activateAbility(land);
        harness.handleListChoice(player1, "PLAINS");

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.PLAINS);
    }

    @Test
    @DisplayName("Choosing Island replaces the target land's type until end of turn")
    void choosingIslandReplacesLandTypeUntilEndOfTurn() {
        Permanent land = addKavuAndLand();

        activateAbility(land);
        harness.handleListChoice(player1, "ISLAND");

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.ISLAND);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).isEmpty();
    }

    @Test
    @DisplayName("Choosing Island removes the target land's printed abilities and changes its mana")
    void choosingIslandReplacesPrintedAbilitiesAndMana() {
        Permanent land = addKavuAndLand();

        activateAbility(land);
        harness.handleListChoice(player1, "ISLAND");

        assertThat(gqs.effectiveLandTypes(gd, land)).containsExactly(CardSubtype.ISLAND);
        assertThat(gqs.hasLostPrintedAbilities(gd, land)).isTrue();
        assertThat(gqs.getOverriddenLandManaColor(gd, land)).isEqualTo(ManaColor.BLUE);
    }

    @Test
    @DisplayName("Activating the ability taps Tundra Kavu as a cost")
    void activatingAbilityTapsSource() {
        Permanent kavu = addCreatureReady(player1, new TundraKavu());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new YavimayaCoast());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, land.getId());

        assertThat(kavu.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability cannot target a non-land permanent")
    void cannotTargetNonLand() {
        addCreatureReady(player1, new TundraKavu());
        Permanent creature = addCreatureReady(player2, new Dodecapod());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("A removed target causes the ability to resolve without a type choice")
    void removedTargetDoesNotOfferChoice() {
        Permanent land = addKavuAndLand();
        harness.activateAbility(player1, 0, null, land.getId());
        gd.playerBattlefields.get(player2.getId()).remove(land);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("The ability resolves even if Tundra Kavu leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent land = addKavuAndLand();
        harness.activateAbility(player1, 0, null, land.getId());
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();
        harness.handleListChoice(player1, "PLAINS");

        assertThat(gqs.effectiveLandTypes(gd, land)).containsExactly(CardSubtype.PLAINS);
    }

    @Test
    @DisplayName("The controller can target their own land and tap it for white without pain")
    void ownLandProducesWhiteManaWithoutPain() {
        addCreatureReady(player1, new TundraKavu());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaCoast());
        harness.forceActivePlayer(player1);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        activateAbility(land);
        harness.handleListChoice(player1, "PLAINS");
        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        harness.assertLife(player1, lifeBefore);
        assertThat(land.isTapped()).isTrue();
    }

    @Test
    @DisplayName("A summoning-sick Tundra Kavu cannot pay the tap cost")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new TundraKavu());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new YavimayaCoast());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }
    private Permanent addKavuAndLand() {
        addCreatureReady(player1, new TundraKavu());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new YavimayaCoast());
        harness.forceActivePlayer(player1);
        return land;
    }

    private void activateAbility(Permanent land) {
        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();
    }
}
