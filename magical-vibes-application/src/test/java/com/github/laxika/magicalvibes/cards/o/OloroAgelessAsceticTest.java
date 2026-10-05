package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OloroAgelessAscetic.class})
class OloroAgelessAsceticTest extends BaseCardTest {

    @Test
    void gainsTwoLifeAtUpkeepOnBattlefield() {
        harness.addToBattlefield(player1, new OloroAgelessAscetic());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void mayPayToDrawAndDrainOpponentsWhenGainingLife() {
        harness.addToBattlefield(player1, new OloroAgelessAscetic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 1));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
    }

    @Test
    void gainsTwoLifeFromTheCommandZone() {
        gd.format = DeckFormat.COMMANDER;
        var oloro = new OloroAgelessAscetic();
        gd.makeCommander(player1.getId(), oloro);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(oloro)));
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
    }

    @Test
    void decliningPaymentDoesNotDrawOrDrain() {
        harness.addToBattlefield(player1, new OloroAgelessAscetic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 4));
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    void gainingMultipleLifeTriggersOnlyOnce() {
        harness.addToBattlefield(player1, new OloroAgelessAscetic());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int opponentLifeBefore = gd.getLife(player2.getId());

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 4));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore - 1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void commandZoneLifeGainDoesNotOfferDrawAndDrain() {
        gd.format = DeckFormat.COMMANDER;
        var oloro = new OloroAgelessAscetic();
        gd.makeCommander(player1.getId(), oloro);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(oloro)));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int lifeBefore = gd.getLife(player1.getId());
        int opponentLifeBefore = gd.getLife(player2.getId());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.getLife(player2.getId())).isEqualTo(opponentLifeBefore);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void commandZoneUpkeepGainsNoLifeIfOloroLeavesBeforeResolution() {
        gd.format = DeckFormat.COMMANDER;
        var oloro = new OloroAgelessAscetic();
        gd.makeCommander(player1.getId(), oloro);
        gd.playerCommandZones.put(player1.getId(), new ArrayList<>(List.of(oloro)));
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        gd.playerCommandZones.get(player1.getId()).remove(oloro);
        gd.playerHands.get(player1.getId()).add(oloro);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void doesNotGainLifeDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new OloroAgelessAscetic());
        int lifeBefore = gd.getLife(player1.getId());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore);
    }

    @Test
    void opponentsLifeGainDoesNotTriggerDrawAndDrain() {
        harness.addToBattlefield(player1, new OloroAgelessAscetic());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 2));

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }
}
