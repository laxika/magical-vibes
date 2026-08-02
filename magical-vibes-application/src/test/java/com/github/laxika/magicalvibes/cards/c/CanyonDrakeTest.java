package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CanyonDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Ability pumps itself +2/+0 and discards a card at random as a cost")
    void pumpsSelfAndDiscardsAtRandom() {
        harness.addToBattlefield(player1, new CanyonDrake());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, "Canyon Drake"), null, null);
        harness.passBothPriorities();

        Permanent drake = findPermanent(player1, "Canyon Drake");
        assertThat(drake.getEffectivePower()).isEqualTo(3);
        assertThat(drake.getEffectiveToughness()).isEqualTo(2);

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Boost wears off at cleanup")
    void boostWearsOffAtCleanup() {
        harness.addToBattlefield(player1, new CanyonDrake());
        harness.setHand(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, "Canyon Drake"), null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent drake = findPermanent(player1, "Canyon Drake");
        assertThat(drake.getPowerModifier()).isEqualTo(0);
        assertThat(drake.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Cannot activate with an empty hand (no card to discard)")
    void cannotActivateWithEmptyHand() {
        harness.addToBattlefield(player1, new CanyonDrake());
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, "Canyon Drake"), null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability can be activated repeatedly, stacking the boost")
    void boostsStack() {
        harness.addToBattlefield(player1, new CanyonDrake());
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        int index = battlefieldIndex(player1, "Canyon Drake");
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, index, null, null);
        harness.passBothPriorities();

        Permanent drake = findPermanent(player1, "Canyon Drake");
        assertThat(drake.getEffectivePower()).isEqualTo(5);
        assertThat(drake.getEffectiveToughness()).isEqualTo(2);
    }

    private int battlefieldIndex(Player player, String cardName) {
        List<Permanent> battlefield = harness.getGameData().playerBattlefields.get(player.getId());
        for (int i = 0; i < battlefield.size(); i++) {
            if (battlefield.get(i).getCard().getName().equals(cardName)) {
                return i;
            }
        }
        throw new IllegalStateException("Permanent not found: " + cardName);
    }
}
