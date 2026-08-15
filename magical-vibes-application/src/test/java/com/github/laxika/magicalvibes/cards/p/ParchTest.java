package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.AzureDrake;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ParchTest extends BaseCardTest {

    @Test
    @DisplayName("The 2-damage mode deals 2 damage to a player")
    void twoDamageModeDealsDamageToPlayer() {
        cast(0, player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The 4-damage mode destroys a blue creature")
    void fourDamageModeDestroysBlueCreature() {
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new AzureDrake());

        cast(1, drake.getId());

        harness.assertNotOnBattlefield(player2, "Azure Drake");
        harness.assertInGraveyard(player2, "Azure Drake");
    }

    @Test
    @DisplayName("The 4-damage mode cannot target a nonblue creature")
    void fourDamageModeRejectsNonblueCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Parch()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(int mode, java.util.UUID targetId) {
        harness.setHand(player1, List.of(new Parch()));
        addMana();
        harness.castInstant(player1, 0, mode, targetId);
        harness.passBothPriorities();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }
}
