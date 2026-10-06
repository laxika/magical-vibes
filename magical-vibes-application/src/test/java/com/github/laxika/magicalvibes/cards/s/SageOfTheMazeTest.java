package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RakdosGuildgate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
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

@CardUsed({SageOfTheMaze.class, Forest.class, RakdosGuildgate.class})
class SageOfTheMazeTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Sage of the Maze adds two mana chosen independently")
    void addsTwoManaInAnyCombinationOfColors() {
        Permanent sage = addCreatureReady(player1, new SageOfTheMaze());

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(sage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Animates a land as a Gate-scaled Citizen until end of turn")
    void animatesLandBasedOnGateCount() {
        addCreatureReady(player1, new SageOfTheMaze());
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player1, new RakdosGuildgate());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isTrue();
        assertThat(gqs.isLand(gd, forest)).isTrue();
        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
        assertThat(gqs.effectiveCreatureSubtypes(gd, forest)).contains(CardSubtype.CITIZEN);
        assertThat(gqs.hasKeyword(gd, forest, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, forest)).isFalse();
    }

    @Test
    @DisplayName("The animation ability only targets lands you control at sorcery speed")
    void animationTargetAndTimingAreRestricted() {
        addCreatureReady(player1, new SageOfTheMaze());
        Permanent ownForest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent opponentForest = harness.addToBattlefieldAndReturn(player2, new Forest());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, ownForest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");

        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, opponentForest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("land you control");
    }

    @Test
    @DisplayName("Tapping a Gate untaps Sage of the Maze")
    void tappingGateUntapsSage() {
        Permanent sage = addCreatureReady(player1, new SageOfTheMaze());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.RED.name());
        harness.handleListChoice(player1, ManaColor.GREEN.name());
        assertThat(sage.isTapped()).isTrue();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();

        assertThat(gate.isTapped()).isTrue();
        assertThat(sage.isTapped()).isFalse();
    }

    @Test
    void gateCountIsDeterminedAtResolutionAndThenRemainsFixed() {
        addCreatureReady(player1, new SageOfTheMaze());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
        harness.addToBattlefield(player2, new RakdosGuildgate());
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).remove(gate);
        harness.addToBattlefield(player1, new RakdosGuildgate());
        harness.addToBattlefield(player1, new RakdosGuildgate());

        assertThat(gqs.getEffectivePower(gd, forest)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, forest)).isEqualTo(4);
    }

    @Test
    void animatingWithNoGatesPutsLandIntoGraveyard() {
        addCreatureReady(player1, new SageOfTheMaze());
        harness.addToBattlefield(player2, new RakdosGuildgate());
        Forest card = new Forest();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, card);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, forest.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(forest);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    @Test
    void untapCostCannotUseTappedOrOpposingGatesOrNonGates() {
        Permanent sage = addCreatureReady(player1, new SageOfTheMaze());
        sage.tap();
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
        gate.tap();
        harness.addToBattlefield(player2, new RakdosGuildgate());
        harness.addToBattlefield(player1, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough untapped permanents");
        assertThat(sage.isTapped()).isTrue();
    }

    @Test
    void untapAbilityWorksForSummoningSickSageAtInstantSpeed() {
        Permanent sage = harness.addToBattlefieldAndReturn(player1, new SageOfTheMaze());
        sage.tap();
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.activateAbility(player1, 0, 2, null, null);

        assertThat(gate.isTapped()).isTrue();
        assertThat(sage.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(sage.isTapped()).isFalse();
    }

    @Test
    void manaAbilityCanProduceTwoManaOfTheSameColorWithoutUsingStack() {
        addCreatureReady(player1, new SageOfTheMaze());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, ManaColor.BLUE.name());
        harness.handleListChoice(player1, ManaColor.BLUE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void animatedGateStillPaysUntapCostAndKeepsItsManaAbility() {
        Permanent sage = addCreatureReady(player1, new SageOfTheMaze());
        Permanent gate = harness.addToBattlefieldAndReturn(player1, new RakdosGuildgate());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, gate.getId());
        harness.passBothPriorities();
        assertThat(gqs.isCreature(gd, gate)).isTrue();
        assertThat(gqs.isLand(gd, gate)).isTrue();
        assertThat(gqs.getEffectivePower(gd, gate)).isEqualTo(2);

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, ManaColor.BLACK.name());
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        gate.untap();

        harness.activateAbility(player1, 0, 2, null, null);
        harness.passBothPriorities();
        assertThat(gate.isTapped()).isTrue();
        assertThat(sage.isTapped()).isFalse();
    }
}
