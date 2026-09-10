package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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

@CardUsed({CanyonDrake.class, Forest.class, Mountain.class})
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

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Pays exactly one random discard before the ability resolves")
    void paysExactlyOneRandomDiscardBeforeResolution() {
        harness.addToBattlefield(player1, new CanyonDrake());
        harness.setHand(player1, List.of(new Forest(), new Mountain()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(player1, "Canyon Drake"), null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(findPermanent(player1, "Canyon Drake").getEffectivePower()).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Canyon Drake").getEffectivePower()).isEqualTo(3);
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
    @DisplayName("Cannot activate without paying the generic mana cost")
    void cannotActivateWithoutMana() {
        harness.addToBattlefield(player1, new CanyonDrake());
        harness.setHand(player1, List.of(new Forest()));

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(player1, "Canyon Drake"), null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
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
        return gd.playerBattlefields.get(player.getId()).indexOf(findPermanent(player, cardName));
    }
}
