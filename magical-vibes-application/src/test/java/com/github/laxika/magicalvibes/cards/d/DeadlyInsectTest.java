package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.e.Exile;
import com.github.laxika.magicalvibes.cards.n.NobleSteeds;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeadlyInsect.class, Exile.class, NobleSteeds.class})
class DeadlyInsectTest extends BaseCardTest {

    @Test
    @DisplayName("Shroud prevents spells from targeting Deadly Insect")
    void spellsCannotTargetDeadlyInsect() {
        Permanent insect = addCreatureReady(player1, new DeadlyInsect());
        insect.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Exile()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, insect.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }

    @Test
    @DisplayName("Shroud prevents abilities from targeting Deadly Insect")
    void abilitiesCannotTargetDeadlyInsect() {
        harness.addToBattlefield(player1, new NobleSteeds());
        Permanent insect = addCreatureReady(player1, new DeadlyInsect());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, insect.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
