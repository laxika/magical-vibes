package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GiantCockroach;
import com.github.laxika.magicalvibes.cards.t.ThornwindFaeries;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantCockroach.class, Parch.class, ThornwindFaeries.class})
class ParchTest extends BaseCardTest {

    @Test
    @DisplayName("The 2-damage mode deals 2 damage to a player")
    void twoDamageModeDealsDamageToPlayer() {
        cast(0, player2.getId());

        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The 2-damage mode deals 2 damage to a creature")
    void twoDamageModeDealsDamageToCreature() {
        Permanent cockroach = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());

        cast(0, cockroach.getId());

        assertThat(cockroach.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("The 4-damage mode destroys a blue creature")
    void fourDamageModeDestroysBlueCreature() {
        Permanent faeries = harness.addToBattlefieldAndReturn(player2, new ThornwindFaeries());

        cast(1, faeries.getId());

        harness.assertNotOnBattlefield(player2, "Thornwind Faeries");
        harness.assertInGraveyard(player2, "Thornwind Faeries");
    }

    @Test
    @DisplayName("The 4-damage mode cannot target a nonblue creature")
    void fourDamageModeRejectsNonblueCreature() {
        Permanent cockroach = harness.addToBattlefieldAndReturn(player2, new GiantCockroach());
        harness.setHand(player1, List.of(new Parch()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, cockroach.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The 4-damage mode cannot target a player")
    void fourDamageModeRejectsPlayer() {
        harness.setHand(player1, List.of(new Parch()));
        addMana();

        assertThatThrownBy(() -> harness.castInstant(player1, 0, 1, player2.getId()))
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
