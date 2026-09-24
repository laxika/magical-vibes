package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.y.YavimayaCoast;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ReefShaman.class, YavimayaCoast.class, Dodecapod.class})
class ReefShamanTest extends BaseCardTest {

    @Test
    @DisplayName("Target land becomes the chosen basic land type until end of turn")
    void targetLandBecomesChosenType() {
        Permanent land = addReefShamanAndLand();

        activateAndChooseIsland(land);

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.ISLAND);
        assertThat(gqs.effectiveLandTypes(gd, land)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The land type override wears off at end of turn")
    void overrideWearsOffAtEndOfTurn() {
        Permanent land = addReefShamanAndLand();

        activateAndChooseIsland(land);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).isEmpty();
        assertThat(gqs.effectiveLandTypes(gd, land)).isEmpty();
    }

    @Test
    @DisplayName("The overridden land produces mana of the chosen type")
    void overriddenLandProducesChosenMana() {
        Permanent land = addReefShamanAndLand();

        activateAndChooseIsland(land);

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(land));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Activating the ability taps Reef Shaman as a cost")
    void activatingAbilityTapsSource() {
        Permanent shaman = addCreatureReady(player1, new ReefShaman());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaCoast());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, land.getId());

        assertThat(shaman.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Becoming a basic land type removes the target land's printed abilities")
    void replacingBasicLandTypeRemovesPrintedAbilities() {
        Permanent land = addReefShamanAndLand();

        activateAndChooseIsland(land);

        assertThat(gs.getEffectiveActivatedAbilities(gd, land)).isEmpty();
    }

    @Test
    @DisplayName("The ability can target an opponent's land")
    void canTargetOpponentsLand() {
        addCreatureReady(player1, new ReefShaman());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new YavimayaCoast());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, land.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(land.getId());

        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        assertThat(gqs.effectiveBasicLandTypes(gd, land)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The ability cannot target a non-land permanent")
    void cannotTargetNonLand() {
        addCreatureReady(player1, new ReefShaman());
        Permanent nonLand = addCreatureReady(player1, new Dodecapod());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonLand.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    private Permanent addReefShamanAndLand() {
        addCreatureReady(player1, new ReefShaman());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new YavimayaCoast());
        harness.forceActivePlayer(player1);
        return land;
    }

    private void activateAndChooseIsland(Permanent land) {
        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");
    }
}
