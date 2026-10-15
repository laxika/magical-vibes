package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BloodMoon;
import com.github.laxika.magicalvibes.cards.v.VividCreek;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TideshaperMystic.class, Forest.class, TurtleshellChangeling.class, VividCreek.class, BloodMoon.class})
class TideshaperMysticTest extends BaseCardTest {

    // ===== Activated ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting a land")
    void activatingAbilityPutsOnStack() {
        addCreatureReady(player1, new TideshaperMystic());
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
    @DisplayName("Resolving prompts for a type-replacing basic land type choice")
    void resolvingPromptsForChoice() {
        addCreatureReady(player1, new TideshaperMystic());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player1);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        harness.activateAbility(player1, 0, null, forestId);
        harness.passBothPriorities();

        var interaction = gd.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        assertThat(interaction.playerId()).isEqualTo(player1.getId());
        assertThat(interaction.options()).containsExactly("PLAINS", "ISLAND", "SWAMP", "MOUNTAIN", "FOREST");
        assertThat(interaction.context()).isInstanceOf(ChoiceContext.AddBasicLandTypeChoice.class);
        assertThat(((ChoiceContext.AddBasicLandTypeChoice) interaction.context()).replacing()).isTrue();
    }

    @Test
    @DisplayName("Activating the ability taps Tideshaper Mystic as a cost")
    void activatingAbilityTapsSource() {
        Permanent mystic = addCreatureReady(player1, new TideshaperMystic());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, forest.getId());

        assertThat(mystic.isTapped()).isTrue();
    }

    // ===== Type replacement (rule 305.7) =====

    @Test
    @DisplayName("Chosen type overrides the land's subtypes to the new basic type only")
    void chosenTypeOverridesSubtypes() {
        Permanent forest = becomeIsland(player1);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);
        assertThat(gqs.isLand(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Replacing removes the land's original type and printed abilities")
    void replacingRemovesOriginalTypeAndAbilities() {
        Permanent forest = becomeIsland(player1);

        assertThat(gqs.effectiveLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);
        assertThat(gqs.hasLostPrintedAbilities(gd, forest)).isTrue();
    }

    @Test
    @DisplayName("Overridden Forest produces blue mana instead of green")
    void overriddenForestProducesBlueMana() {
        Permanent forest = becomeIsland(player1);

        int forestIndex = gd.playerBattlefields.get(player1.getId()).indexOf(forest);
        harness.tapPermanent(player1, forestIndex);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(0);
    }

    // ===== Until end of turn =====

    @Test
    @DisplayName("Override is cleared at end of turn")
    void overrideClearedAtEndOfTurn() {
        Permanent forest = becomeIsland(player1);
        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.ISLAND);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UNTAP);

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).containsExactly(CardSubtype.FOREST);
    }

    // ===== Targeting / timing restrictions =====

    @Test
    @DisplayName("Can target a land controlled by the opponent")
    void canTargetOpponentLand() {
        addCreatureReady(player1, new TideshaperMystic());
        harness.addToBattlefield(player2, new Forest());
        harness.forceActivePlayer(player1);
        UUID opponentForestId = harness.getPermanentId(player2, "Forest");

        harness.activateAbility(player1, 0, null, opponentForestId);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(opponentForestId);
    }

    @Test
    @DisplayName("Cannot activate during the opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new TideshaperMystic());
        harness.addToBattlefield(player1, new Forest());
        harness.forceActivePlayer(player2);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-land permanent")
    void cannotTargetNonLand() {
        addCreatureReady(player1, new TideshaperMystic());
        harness.addToBattlefield(player1, new Forest()); // valid target so the ability is activatable
        harness.addToBattlefield(player1, new TurtleshellChangeling());
        harness.forceActivePlayer(player1);
        UUID creatureId = harness.getPermanentId(player1, "Turtleshell Changeling");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a land");
    }

    @ParameterizedTest
    @CsvSource({"PLAINS, WHITE", "ISLAND, BLUE", "SWAMP, BLACK", "MOUNTAIN, RED", "FOREST, GREEN"})
    @DisplayName("Every basic land type choice grants its corresponding mana ability")
    void everyBasicLandTypeProducesCorrespondingMana(CardSubtype subtype, ManaColor color) {
        addCreatureReady(player1, new TideshaperMystic());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, subtype.name());

        assertThat(gqs.effectiveLandTypes(gd, forest)).containsExactly(subtype);
        harness.tapPermanent(player1, 1);
        for (ManaColor candidate : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(candidate))
                    .isEqualTo(candidate == color ? 1 : 0);
        }
    }

    @Test
    @DisplayName("Can activate during your own end step")
    void canActivateDuringOwnEndStep() {
        addCreatureReady(player1, new TideshaperMystic());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player1, 0, null, forest.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "SWAMP");

        assertThat(gqs.effectiveLandTypes(gd, forest)).containsExactly(CardSubtype.SWAMP);
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new TideshaperMystic());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A later Blood Moon overrides Mystic's earlier change to a nonbasic land")
    void laterBloodMoonOverridesEarlierTypeChange() {
        addCreatureReady(player1, new TideshaperMystic());
        Permanent creek = harness.addToBattlefieldAndReturn(player1, new VividCreek());
        harness.forceActivePlayer(player1);

        harness.activateAbility(player1, 0, null, creek.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "ISLAND");
        assertThat(gqs.effectiveLandTypes(gd, creek)).containsExactly(CardSubtype.ISLAND);

        harness.castFromHand(player1, new BloodMoon(), "{2}{R}");
        harness.passBothPriorities();

        assertThat(gqs.effectiveLandTypes(gd, creek)).containsExactly(CardSubtype.MOUNTAIN);
        harness.tapPermanent(player1, 1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    /** Adds a Tideshaper Mystic + Forest for {@code player}, then makes the Forest become an Island. */
    private Permanent becomeIsland(com.github.laxika.magicalvibes.model.Player player) {
        addCreatureReady(player, new TideshaperMystic());
        Permanent forest = harness.addToBattlefieldAndReturn(player, new Forest());
        harness.forceActivePlayer(player);

        harness.activateAbility(player, 0, null, forest.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player, "ISLAND");

        return forest;
    }
}
