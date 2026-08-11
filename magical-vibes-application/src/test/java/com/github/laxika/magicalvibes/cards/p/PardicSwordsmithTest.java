package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class PardicSwordsmithTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability randomly discards a card and gives +2/+0")
    void discardsAndBoosts() {
        harness.addToBattlefield(player1, new PardicSwordsmith());
        Permanent swordsmith = findPermanent(player1, "Pardic Swordsmith");
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(swordsmith.getPowerModifier()).isEqualTo(2);
        assertThat(swordsmith.getToughnessModifier()).isEqualTo(0);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new PardicSwordsmith());
        Permanent swordsmith = findPermanent(player1, "Pardic Swordsmith");
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(swordsmith.getPowerModifier()).isEqualTo(0);
        assertThat(swordsmith.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The ability cannot be activated with an empty hand")
    void cannotActivateWithEmptyHand() {
        harness.addToBattlefield(player1, new PardicSwordsmith());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
