package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RainOfFilthTest extends BaseCardTest {

    @Test
    @DisplayName("Lands you control gain a sacrifice-for-black-mana ability until end of turn")
    void grantsSacrificeForBlackManaAbility() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of(new RainOfFilth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        harness.activateAbility(player1, 0, 0, null, null);

        harness.assertInGraveyard(player1, "Forest");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The granted ability is removed at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.setHand(player1, List.of(new RainOfFilth()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(forest.getTemporaryActivatedAbilities()).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(forest.getTemporaryActivatedAbilities()).isEmpty();
    }
}
