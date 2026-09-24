package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.WallOfMulch;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({Standardize.class, WallOfMulch.class})
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

    private void castStandardize(Player caster) {
        harness.setHand(caster, List.of(new Standardize()));
        harness.addMana(caster, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(caster, 0);
    }
}
