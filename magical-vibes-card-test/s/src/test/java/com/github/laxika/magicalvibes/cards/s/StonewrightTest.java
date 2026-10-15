package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.Cloudshift;
import com.github.laxika.magicalvibes.cards.f.FarbogExplorer;
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

@CardUsed({Stonewright.class, FarbogExplorer.class, Cloudshift.class})
class StonewrightTest extends BaseCardTest {

    private Permanent castAndPairWithExplorer() {
        Permanent explorer = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());
        harness.castFromHand(player1, new Stonewright(), "{R}");
        harness.passBothPriorities(); // resolve spell -> soulbond may on stack
        harness.passBothPriorities(); // resolve may -> prompt
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, explorer.getId());
        return explorer;
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
        Permanent explorer = castAndPairWithExplorer();
        Permanent stonewright = findStonewright();

        assertThat(stonewright.getPairedWithId()).isEqualTo(explorer.getId());
        assertThat(explorer.getPairedWithId()).isEqualTo(stonewright.getId());
    }

    @Test
    @DisplayName("While paired, Stonewright can pump itself for {R}")
    void pairedStonewrightCanPumpSelf() {
        castAndPairWithExplorer();
        Permanent stonewright = findStonewright();

        activatePump(stonewright);

        assertThat(gqs.getEffectivePower(gd, stonewright)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stonewright)).isEqualTo(1);
    }

    @Test
    @DisplayName("While paired, the partner can pump itself for {R}")
    void pairedPartnerCanPumpSelf() {
        Permanent explorer = castAndPairWithExplorer();

        activatePump(explorer);

        assertThat(gqs.getEffectivePower(gd, explorer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, explorer)).isEqualTo(3);
    }

    @Test
    @DisplayName("The pump wears off at end of turn")
    void pumpWearsOff() {
        castAndPairWithExplorer();
        Permanent stonewright = findStonewright();

        activatePump(stonewright);
        assertThat(gqs.getEffectivePower(gd, stonewright)).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.passUntil(player2, TurnStep.UPKEEP);

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
        Permanent explorer = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());
        harness.castFromHand(player1, new Stonewright(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent stonewright = findStonewright();
        assertThat(stonewright.getPairedWithId()).isNull();
        assertThat(explorer.getPairedWithId()).isNull();
        assertThat(gd.interaction.permanentChoiceContext()).isNull();
    }

    @Test
    @DisplayName("Soulbond does not trigger when Stonewright enters without another creature")
    void enteringAloneDoesNotTriggerSoulbond() {
        harness.castFromHand(player1, new Stonewright(), "{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(findStonewright().getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("Soulbond pairs with another creature entering after Stonewright")
    void pairsWithEnteringCreature() {
        Permanent stonewright = harness.addToBattlefieldAndReturn(player1, new Stonewright());
        harness.castFromHand(player1, new FarbogExplorer(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        Permanent explorer = findPermanent(player1, "Farbog Explorer");

        assertThat(stonewright.getPairedWithId()).isEqualTo(explorer.getId());
        assertThat(explorer.getPairedWithId()).isEqualTo(stonewright.getId());
        activatePump(explorer);
        assertThat(gqs.getEffectivePower(gd, explorer)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, stonewright)).isEqualTo(1);
    }

    @Test
    @DisplayName("Declining pairing with an entering creature leaves both unpaired")
    void declinesEnteringCreature() {
        Permanent stonewright = harness.addToBattlefieldAndReturn(player1, new Stonewright());
        harness.castFromHand(player1, new FarbogExplorer(), "{2}{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(stonewright.getPairedWithId()).isNull();
        assertThat(findPermanent(player1, "Farbog Explorer").getPairedWithId()).isNull();
    }

    @Test
    @DisplayName("Repeated pumps affect only the creature activating the ability")
    void pumpsAreIndependentAndCumulative() {
        Permanent explorer = castAndPairWithExplorer();
        Permanent stonewright = findStonewright();

        activatePump(explorer);
        activatePump(explorer);
        activatePump(stonewright);

        assertThat(gqs.getEffectivePower(gd, explorer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, explorer)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, stonewright)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, stonewright)).isEqualTo(1);
    }

    @Test
    @DisplayName("A partner's activated pump resolves after Stonewright leaves the pair")
    void activatedPumpSurvivesPairBreaking() {
        Permanent explorer = castAndPairWithExplorer();
        Permanent stonewright = findStonewright();
        harness.addMana(player1, ManaColor.RED, 1);
        int index = gd.playerBattlefields.get(player1.getId()).indexOf(explorer);
        harness.activateAbility(player1, index, 0, null, null);
        harness.setHand(player1, List.of(new Cloudshift()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castInstant(player1, 0, stonewright.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(explorer.getPairedWithId()).isNull();
        assertThat(gqs.getEffectivePower(gd, explorer)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, findStonewright())).isEqualTo(1);
        harness.addMana(player1, ManaColor.RED, 1);
        assertThatThrownBy(() -> harness.activateAbility(player1, index, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
