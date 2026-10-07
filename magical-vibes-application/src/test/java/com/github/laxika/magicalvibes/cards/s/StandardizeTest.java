package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WallOfMulch;
import com.github.laxika.magicalvibes.cards.x.Xenograft;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Standardize.class, WallOfMulch.class, Xenograft.class})
class StandardizeTest extends BaseCardTest {

    @Test
    @DisplayName("All creatures on the battlefield become the chosen type")
    void allCreaturesBecomeChosenType() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new WallOfMulch());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new WallOfMulch());

        castStandardize(player1);
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, ownCreature)).containsExactly(CardSubtype.GOBLIN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opposingCreature)).containsExactly(CardSubtype.GOBLIN);
    }

    @Test
    @DisplayName("The chosen type wears off at end of turn")
    void chosenTypeWearsOffAtEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WallOfMulch());

        castStandardize(player1);
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.GOBLIN);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.WALL);
    }

    @Test
    @DisplayName("Wall cannot be chosen as the creature type")
    void wallCannotBeChosenAsCreatureType() {
        castStandardize(player1);

        assertThatThrownBy(() -> harness.handleListChoice(player1, CardSubtype.WALL.name()))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    @DisplayName("Creatures entering after resolution retain their creature types")
    void laterEntrantsAreUnaffected() {
        Permanent existing = harness.addToBattlefieldAndReturn(player1, new WallOfMulch());

        castStandardize(player1);
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        Permanent later = harness.addToBattlefieldAndReturn(player2, new WallOfMulch());

        assertThat(gqs.effectiveCreatureSubtypes(gd, existing)).containsExactly(CardSubtype.GOBLIN);
        assertThat(gqs.effectiveCreatureSubtypes(gd, later)).containsExactly(CardSubtype.WALL);
    }

    @Test
    @DisplayName("A second Standardize replaces the first chosen type")
    void laterResolutionReplacesEarlierType() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new WallOfMulch());

        castStandardize(player1);
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        castStandardize(player1);
        harness.handleListChoice(player1, CardSubtype.ELF.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, creature)).containsExactly(CardSubtype.ELF);
    }

    @Test
    @DisplayName("A later Xenograft adds its chosen type after Standardize")
    void laterStaticTypeGrantIsAppliedAfterStandardize() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new WallOfMulch());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new WallOfMulch());

        castStandardize(player1);
        harness.handleListChoice(player1, CardSubtype.GOBLIN.name());
        harness.castFromHand(player1, new Xenograft(), "{4}{U}");
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.ELF.name());

        assertThat(gqs.effectiveCreatureSubtypes(gd, ownCreature))
                .containsExactlyInAnyOrder(CardSubtype.GOBLIN, CardSubtype.ELF);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opposingCreature)).containsExactly(CardSubtype.GOBLIN);
    }

    private void castStandardize(Player caster) {
        harness.castFromHand(caster, new Standardize(), "{U}{U}");
        harness.passBothPriorities();
    }
}
