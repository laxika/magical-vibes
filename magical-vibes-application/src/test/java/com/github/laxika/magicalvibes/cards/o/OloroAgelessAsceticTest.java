package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OloroAgelessAscetic.class})
class OloroAgelessAsceticTest extends BaseCardTest {

    @Test
    @DisplayName("Gains 2 life at the beginning of its controller's upkeep")
    void gainsLifeOnUpkeep() {
        harness.addToBattlefield(player1, new OloroAgelessAscetic());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Gains 2 life from the command zone at upkeep")
    void gainsLifeFromCommandZoneOnUpkeep() {
        OloroAgelessAscetic oloro = new OloroAgelessAscetic();
        gd.playerCommandZones.get(player1.getId()).add(oloro);
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    @DisplayName("Command-zone upkeep trigger does nothing after Oloro leaves the command zone")
    void commandZoneTriggerChecksSourceStillInCommandZone() {
        OloroAgelessAscetic oloro = new OloroAgelessAscetic();
        gd.playerCommandZones.get(player1.getId()).add(oloro);
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        gd.playerCommandZones.get(player1.getId()).remove(oloro);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    @DisplayName("Paying {1} after gaining life draws a card and makes each opponent lose 1 life")
    void paysToDrawAndDrainOpponents() {
        harness.addToBattlefield(player1, new OloroAgelessAscetic());
        harness.setHand(player1, java.util.List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    @DisplayName("Declining the payment does not draw or drain")
    void decliningPaymentDoesNothing() {
        harness.addToBattlefield(player1, new OloroAgelessAscetic());
        harness.setHand(player1, java.util.List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

}
