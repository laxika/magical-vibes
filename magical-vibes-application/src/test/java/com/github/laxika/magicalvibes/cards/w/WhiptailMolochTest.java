package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WhiptailMoloch.class, AirElemental.class, GrizzlyBears.class})
class WhiptailMolochTest extends BaseCardTest {

    @Test
    @DisplayName("ETB deals 3 damage to target creature you control")
    void etbDealsThreeDamageToOwnCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        castWhiptailMoloch(elemental.getId());

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(elemental.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Whiptail Moloch");
    }

    @Test
    @DisplayName("ETB cannot target an opponent's creature")
    void etbCannotTargetOpponentCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID opponentCreature = harness.getPermanentId(player2, "Grizzly Bears");

        harness.setHand(player1, List.of(new WhiptailMoloch()));
        addWhiptailMana();

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, opponentCreature, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature you control");
    }

    @Test
    @DisplayName("ETB does nothing when its target leaves before resolution")
    void etbDoesNothingWhenTargetLeavesBeforeResolution() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player1, new AirElemental());
        castWhiptailMoloch(elemental.getId());

        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).remove(elemental);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Whiptail Moloch");
    }

    private void castWhiptailMoloch(UUID targetId) {
        harness.setHand(player1, List.of(new WhiptailMoloch()));
        addWhiptailMana();
        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
    }

    private void addWhiptailMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
    }
}
