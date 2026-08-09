package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CarnageTyrant;
import com.github.laxika.magicalvibes.cards.l.LeylineOfSanctity;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DetectionTowerTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping it adds one colorless mana")
    void tappingAddsColorlessMana() {
        Permanent tower = new Permanent(new DetectionTower());
        tower.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(tower);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(tower.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Its controller can target an opponent with hexproof")
    void controllerCanTargetOpponentWithHexproof() {
        harness.addToBattlefield(player1, new DetectionTower());
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        activateHexproofIgnore(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Its controller can target an opponent's hexproof creature")
    void controllerCanTargetOpponentHexproofCreature() {
        harness.addToBattlefield(player1, new DetectionTower());
        Permanent tyrant = harness.addToBattlefieldAndReturn(player2, new CarnageTyrant());
        activateHexproofIgnore(player1);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, tyrant.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player2, "Carnage Tyrant");
    }

    @Test
    @DisplayName("Without activating it, an opponent with hexproof cannot be targeted")
    void opponentHexproofStillBlocksWithoutActivation() {
        harness.addToBattlefield(player1, new DetectionTower());
        harness.addToBattlefield(player2, new LeylineOfSanctity());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    private void activateHexproofIgnore(Player player) {
        harness.addMana(player, ManaColor.COLORLESS, 1);
        harness.activateAbility(player, 0, 1, null, null);
        harness.passBothPriorities();
    }
}
