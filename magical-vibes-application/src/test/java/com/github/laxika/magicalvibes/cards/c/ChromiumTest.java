package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ChromiumTest extends BaseCardTest {

    @Test
    @DisplayName("Rampage 2 gives Chromium +2/+2 with two blockers")
    void rampageWithTwoBlockers() {
        Permanent chromium = new Permanent(new Chromium());
        chromium.setSummoningSick(false);
        chromium.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(chromium);
        addReadyBears();
        addReadyBears();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));
        harness.passBothPriorities();

        assertThat(chromium.getPowerModifier()).isEqualTo(2);
        assertThat(chromium.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining the upkeep payment sacrifices Chromium")
    void declineUpkeepPaymentSacrificesChromium() {
        harness.addToBattlefield(player1, new Chromium());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Chromium");
    }

    @Test
    @DisplayName("Paying the upkeep cost keeps Chromium on the battlefield")
    void payingUpkeepCostKeepsChromium() {
        harness.addToBattlefield(player1, new Chromium());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertOnBattlefield(player1, "Chromium");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    private void addReadyBears() {
        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);
    }
}
