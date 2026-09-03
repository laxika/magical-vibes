package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.b.BalduvianBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcishFarmer.class, Forest.class, BalduvianBears.class})
class OrcishFarmerTest extends BaseCardTest {

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a land")
    void activatingAbilityPutsOnStack() {
        Permanent farmer = addCreatureReady(player1, new OrcishFarmer());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(forestId);
        assertThat(farmer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving makes the target land become a Swamp, overriding its subtypes")
    void resolvingOverridesSubtypesToSwamp() {
        Permanent forest = becomeSwamp(player1);

        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("Overridden Forest produces black mana instead of green")
    void overriddenForestProducesBlackMana() {
        becomeSwamp(player1);

        int forestIndex = gd.playerBattlefields.get(player1.getId())
                .indexOf(gqs.findPermanentById(gd, harness.getPermanentId(player1, "Forest")));
        gs.tapPermanent(gd, player1, forestIndex);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    @Test
    @DisplayName("Override survives cleanup and the target land controller's untap step")
    void overrideSurvivesEndOfTurn() {
        Permanent forest = becomeSwamp(player1);

        advanceThroughCleanupToUntap(player2);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("Override is cleared at the controller's next untap step")
    void overrideClearedAtNextUntapStep() {
        Permanent forest = becomeSwamp(player1);

        advanceThroughCleanupToUntap(player2);
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);

        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("Can target a land controlled by the opponent")
    void canTargetOpponentLand() {
        addCreatureReady(player1, new OrcishFarmer());
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player1);
        UUID opponentForestId = harness.getPermanentId(player2, "Forest");

        harness.activateAbility(player1, 0, null, opponentForestId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(opponentForestId);
    }

    @Test
    @DisplayName("Cannot target a non-land permanent")
    void cannotTargetNonLand() {
        addCreatureReady(player1, new OrcishFarmer());
        harness.addToBattlefield(player1, new Forest()); // valid target so the ability is activatable
        harness.addToBattlefield(player1, new BalduvianBears());
        harness.forceActivePlayer(player1);
        UUID bearsId = harness.getPermanentId(player1, "Balduvian Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @Test
    @DisplayName("Override expires at the targeted land controller's next untap step")
    void overrideExpiresAtTargetControllerUntapStep() {
        addCreatureReady(player2, new OrcishFarmer());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player2);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player2, 0, null, forestId);
        harness.passBothPriorities();

        Permanent forest = gqs.findPermanentById(gd, forestId);
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);

        advanceThroughCleanupToUntap(player1);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
    }

    /** Adds an Orcish Farmer + Forest for {@code player}, then makes the Forest become a Swamp. */
    private Permanent becomeSwamp(Player player) {
        addCreatureReady(player, new OrcishFarmer());
        harness.addToBattlefield(player, new Forest());
        harness.forceActivePlayer(player);
        UUID forestId = harness.getPermanentId(player, "Forest");

        harness.activateAbility(player, 0, null, forestId);
        harness.passBothPriorities();

        return gqs.findPermanentById(gd, forestId);
    }

    /** Advances through cleanup and stops at the requested player's untap step. */
    private void advanceThroughCleanupToUntap(Player activePlayer) {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(activePlayer, TurnStep.UNTAP);
    }
}
