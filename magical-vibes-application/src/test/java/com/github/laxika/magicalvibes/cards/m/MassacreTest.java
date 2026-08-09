package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MassacreTest extends BaseCardTest {

    @Test
    @DisplayName("Gives all creatures -2/-2 when cast for free with the required lands")
    void castsForFreeWithRequiredLands() {
        Permanent ownCreature = addCreatureReady(player1, new AirElemental());
        Permanent opponentCreature = addCreatureReady(player2, new AirElemental());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Plains());
        harness.setHand(player1, List.of(new Massacre()));

        harness.castWithAlternateCost(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cannot use the free cast without an opponent's Plains and your Swamp")
    void cannotCastForFreeWithoutRequiredLands() {
        harness.addToBattlefield(player1, new Swamp());
        harness.setHand(player1, List.of(new Massacre()));

        assertThatThrownBy(() -> harness.castWithAlternateCost(player1, 0, (java.util.UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be cast normally when the free-cast condition is not met")
    void castsNormallyWithoutRequiredLands() {
        Permanent creature = addCreatureReady(player2, new AirElemental());
        harness.setHand(player1, List.of(new Massacre()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
    }

    @Test
    @DisplayName("The -2/-2 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player2, new AirElemental());
        harness.setHand(player1, List.of(new Massacre()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, (java.util.UUID) null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(4);
    }
}
