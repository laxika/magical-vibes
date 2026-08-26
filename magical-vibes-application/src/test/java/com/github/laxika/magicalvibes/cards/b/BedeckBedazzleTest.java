package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.e.EvolvingWilds;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BedeckBedazzleTest extends BaseCardTest {

    private static final int BEDECK = 0;
    private static final int BEDAZZLE = 1;
    private static final int FUSE = 2;

    @Test
    @DisplayName("Bedeck gives the targeted creature +3/-3 until end of turn")
    void bedeckBoostsAndWeakensCreature() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new BedeckBedazzle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, BEDECK, elemental.getId());
        harness.passBothPriorities();

        assertThat(elemental.getEffectivePower()).isEqualTo(7);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Bedeck wears off at end of turn")
    void bedeckWearsOffAtEndOfTurn() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());

        harness.setHand(player1, List.of(new BedeckBedazzle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castInstant(player1, 0, BEDECK, elemental.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(elemental.getEffectivePower()).isEqualTo(4);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Bedazzle destroys a nonbasic land and deals 2 damage to an opponent")
    void bedazzleDestroysLandAndDamagesOpponent() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds());

        harness.setHand(player1, List.of(new BedeckBedazzle()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalInstant(player1, 0, BEDAZZLE, List.of(land.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Evolving Wilds");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Fuse resolves Bedeck before Bedazzle")
    void fuseResolvesBothHalvesInOrder() {
        Permanent elemental = harness.addToBattlefieldAndReturn(player2, new AirElemental());
        Permanent land = harness.addToBattlefieldAndReturn(player2, new EvolvingWilds());

        harness.setHand(player1, List.of(new BedeckBedazzle()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castModalInstant(player1, 0, FUSE,
                List.of(elemental.getId(), land.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(elemental.getEffectivePower()).isEqualTo(7);
        assertThat(elemental.getEffectiveToughness()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Evolving Wilds");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Bedazzle cannot target a basic land")
    void bedazzleCannotTargetBasicLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());

        harness.setHand(player1, List.of(new BedeckBedazzle()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castModalInstant(
                player1, 0, BEDAZZLE, List.of(land.getId(), player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Bedeck cannot target a player")
    void bedeckCannotTargetPlayer() {
        harness.setHand(player1, List.of(new BedeckBedazzle()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, BEDECK, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
