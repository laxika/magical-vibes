package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.PhantasmalTerrain;
import com.github.laxika.magicalvibes.cards.s.Stasis;
import com.github.laxika.magicalvibes.cards.s.SulfurousSprings;
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

@CardUsed({OrcishFarmer.class, Forest.class, GrizzlyBears.class, PhantasmalTerrain.class,
        Stasis.class, SulfurousSprings.class})
class OrcishFarmerTest extends BaseCardTest {
    @Test
    @DisplayName("Activating ability puts it on the stack targeting a land")
    void activatingAbilityPutsOnStack() {
        addCreatureReady(player1, new OrcishFarmer());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(forestId);
    }

    @Test
    @DisplayName("Activating the ability taps Orcish Farmer as a cost")
    void activatingAbilityTapsSource() {
        Permanent farmer = addCreatureReady(player1, new OrcishFarmer());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, forest.getId());

        assertThat(farmer.isTapped()).isTrue();
    }
    @Test
    @DisplayName("Resolving makes the target land become a Swamp, overriding its subtypes")
    void resolvingOverridesSubtypesToSwamp() {
        Permanent forest = becomeSwamp(player1);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Overridden Forest produces black mana instead of green")
    void overriddenForestProducesBlackMana() {
        Permanent forest = becomeSwamp(player1);

        harness.tapPermanent(player1, gd.playerBattlefields.get(player1.getId()).indexOf(forest));

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }
    @Test
    @DisplayName("Override survives end-of-turn cleanup (unlike an until-end-of-turn override)")
    void overrideSurvivesEndOfTurn() {
        Permanent forest = becomeSwamp(player1);

        advanceToNextTurn(player1);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("Override is cleared at the controller's next untap step")
    void overrideClearedAtNextUntapStep() {
        Permanent forest = becomeSwamp(player1);

        advanceToNextTurn(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
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
    @DisplayName("An opponent's land lasts through the source controller's next untap")
    void opponentLandExpiresOnItsOwnControllerUntap() {
        addCreatureReady(player1, new OrcishFarmer());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UNTAP);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UNTAP);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
    }

    @Test
    @DisplayName("Cannot target a non-land permanent")
    void cannotTargetNonLand() {
        addCreatureReady(player1, new OrcishFarmer());
        harness.addToBattlefield(player1, new Forest()); // valid target so the ability is activatable
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }
    @Test
    @DisplayName("The Swamp effect survives a skipped untap step")
    void overrideSurvivesSkippedUntapStep() {
        Permanent forest = becomeSwamp(player1);
        harness.addToBattlefield(player2, new Stasis());

        advanceToNextTurn(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.UPKEEP);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("A later Phantasmal Terrain overrides the Swamp effect")
    void laterLandTypeEffectWins() {
        Permanent forest = becomeSwamp(player1);
        harness.setHand(player1, List.of(new PhantasmalTerrain()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);
    }

    @Test
    @DisplayName("The Swamp effect overrides an earlier Phantasmal Terrain")
    void laterFarmerEffectWins() {
        addCreatureReady(player1, new OrcishFarmer());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new PhantasmalTerrain()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, forest.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("A nonbasic land loses its printed mana abilities while it is a Swamp")
    void nonbasicLandProducesBlackWithoutDamage() {
        addCreatureReady(player1, new OrcishFarmer());
        Permanent land = harness.addToBattlefieldAndReturn(player1, new SulfurousSprings());
        harness.forceActivePlayer(player1);
        harness.activateAbility(player1, 0, null, land.getId());
        harness.passBothPriorities();

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("A summoning-sick Farmer cannot pay the tap cost")
    void summoningSicknessPreventsActivation() {
        Permanent farmer = harness.addToBattlefieldAndReturn(player1, new OrcishFarmer());
        farmer.setSummoningSick(true);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(farmer.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    /** Adds an Orcish Farmer + Forest for {@code player}, then makes the Forest become a Swamp. */
    private Permanent becomeSwamp(Player player) {
        addCreatureReady(player, new OrcishFarmer());
        Permanent forest = harness.addToBattlefieldAndReturn(player, new Forest());
        harness.forceActivePlayer(player);

        harness.activateAbility(player, 0, null, forest.getId());
        harness.passBothPriorities();

        return forest;
    }

    private void advanceToNextTurn(Player currentActivePlayer) {
        Player nextActivePlayer = currentActivePlayer == player1 ? player2 : player1;
        harness.forceActivePlayer(currentActivePlayer);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(nextActivePlayer, TurnStep.UNTAP);
    }
}
