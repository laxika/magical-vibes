package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class JeskaiDevoteeTest extends BaseCardTest {

    @Test
    @DisplayName("The second spell gives Jeskai Devotee +1/+1 until end of turn")
    void secondSpellBoostsUntilEndOfTurn() {
        Permanent devotee = addCreatureReady(player1, new JeskaiDevotee());
        int initialPower = devotee.getEffectivePower();

        harness.setHand(player1, List.of(new LightningBolt(), new LightningBolt(), new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(devotee.getEffectivePower()).isEqualTo(initialPower);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(devotee.getEffectivePower()).isEqualTo(initialPower + 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(devotee.getEffectivePower()).isEqualTo(initialPower + 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(devotee.getEffectivePower()).isEqualTo(initialPower);
    }

    @Test
    @DisplayName("The mana ability adds a chosen Jeskai color and can be activated only once each turn")
    void manaAbilityAddsChosenColorAndIsLimitedToOnceEachTurn() {
        harness.addToBattlefield(player1, new JeskaiDevotee());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
