package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StonewrightTest extends BaseCardTest {

    private Permanent castAndPairWithBears() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Stonewright()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, bears.getId());
        return bears;
    }

    private Permanent findStonewright() {
        return findPermanent(player1, "Stonewright");
    }

    private void activatePump(Permanent permanent) {
        harness.addMana(player1, ManaColor.RED, 1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
        harness.activateAbility(player1, index, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Soulbond ETB pairs Stonewright with another unpaired creature")
    void soulbondPairsOnEnter() {
        Permanent bears = castAndPairWithBears();
        Permanent stonewright = findStonewright();

        assertThat(stonewright.getPairedWithId()).isEqualTo(bears.getId());
        assertThat(bears.getPairedWithId()).isEqualTo(stonewright.getId());
    }

    @Test
    @DisplayName("While paired, Stonewright can pump itself for {R}")
    void pairedStonewrightCanPumpSelf() {
        castAndPairWithBears();
        Permanent stonewright = findStonewright();

        activatePump(stonewright);

        assertThat(gqs.getEffectivePower(gd, stonewright)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stonewright)).isEqualTo(1);
    }

    @Test
    @DisplayName("While paired, the partner can pump itself for {R}")
    void pairedPartnerCanPumpSelf() {
        Permanent bears = castAndPairWithBears();

        activatePump(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("The pump wears off at end of turn")
    void pumpWearsOff() {
        castAndPairWithBears();
        Permanent stonewright = findStonewright();

        activatePump(stonewright);
        assertThat(gqs.getEffectivePower(gd, stonewright)).isEqualTo(2);

        stonewright.resetModifiers();
        gd.expireEndOfTurnFloatingEffects();

        assertThat(gqs.getEffectivePower(gd, stonewright)).isEqualTo(1);
    }

    @Test
    @DisplayName("Unpaired Stonewright does not have the pump ability")
    void unpairedCannotPump() {
        harness.addToBattlefield(player1, new Stonewright());
        Permanent stonewright = findStonewright();
        harness.addMana(player1, ManaColor.RED, 1);

        int index = gd.playerBattlefields.get(player1.getId()).indexOf(stonewright);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Declining soulbond leaves both unpaired without the ability")
    void decliningLeavesUnpaired() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Stonewright()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent stonewright = findStonewright();
        assertThat(stonewright.getPairedWithId()).isNull();
        assertThat(bears.getPairedWithId()).isNull();
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }
}
