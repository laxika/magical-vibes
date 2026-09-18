package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.DeckFormat;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(OloroAgelessAscetic.class)
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
}
