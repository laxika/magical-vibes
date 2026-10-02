package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BeaconOfImmortality;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritOfTheHearth.class, BeaconOfImmortality.class, Shock.class})
class SpiritOfTheHearthTest extends BaseCardTest {

    @Test
    @DisplayName("Controller has hexproof while Spirit of the Hearth is on the battlefield")
    void controllerHasHexproof() {
        harness.addToBattlefield(player1, new SpiritOfTheHearth());

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isTrue();
    }

    @Test
    @DisplayName("Opponent cannot target the controller with a spell")
    void opponentCannotTargetController() {
        harness.addToBattlefield(player1, new SpiritOfTheHearth());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new BeaconOfImmortality()));
        harness.addMana(player2, ManaColor.WHITE, 6);

        assertThatThrownBy(() -> harness.castInstant(player2, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("hexproof");
    }

    @Test
    @DisplayName("Controller can still target themselves")
    void canTargetSelf() {
        harness.addToBattlefield(player1, new SpiritOfTheHearth());
        harness.setLife(player1, 20);
        harness.setHand(player1, List.of(new BeaconOfImmortality()));
        harness.addMana(player1, ManaColor.WHITE, 6);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(40);
    }

    @Test
    @DisplayName("Opponent can still target Spirit of the Hearth itself")
    void permanentItselfCanBeTargeted() {
        Permanent spirit = harness.addToBattlefieldAndReturn(player1, new SpiritOfTheHearth());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, spirit.getId());
        harness.passBothPriorities();

        assertThat(spirit.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller loses hexproof once Spirit of the Hearth leaves the battlefield")
    void hexproofGoneAfterSpiritLeaves() {
        SpiritOfTheHearth spirit = new SpiritOfTheHearth();
        harness.addToBattlefield(player1, spirit);

        Permanent perm = findPermanent(player1, "Spirit of the Hearth");
        gd.playerBattlefields.get(player1.getId()).remove(perm);
        gd.playerGraveyards.get(player1.getId()).add(spirit);

        assertThat(gqs.playerHasHexproof(gd, player1.getId())).isFalse();
    }
}
