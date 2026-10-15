package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(FiskTower.class)
class FiskTowerTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and gains 1 life")
    void entersTappedAndGainsLife() {
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new FiskTower()));

        harness.playLand(player1, 0);

        Permanent tower = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(tower.isTapped()).isTrue();

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Mana ability adds white mana when white is chosen")
    void manaAbilityAddsWhiteMana() {
        Permanent tower = addReadyTower();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "WHITE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(1);
        assertThat(tower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Mana ability adds black mana when black is chosen")
    void manaAbilityAddsBlackMana() {
        Permanent tower = addReadyTower();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.handleListChoice(player1, "BLACK");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(tower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Life gain waits for the enters trigger to resolve")
    void lifeGainWaitsForTriggerResolution() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new FiskTower()));

        harness.playLand(player1, 0);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 20);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Cannot produce mana while tapped after entering")
    void cannotActivateWhileTapped() {
        harness.setHand(player1, List.of(new FiskTower()));
        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("tapped");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    private Permanent addReadyTower() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new FiskTower());
        tower.setSummoningSick(false);
        return tower;
    }
}
