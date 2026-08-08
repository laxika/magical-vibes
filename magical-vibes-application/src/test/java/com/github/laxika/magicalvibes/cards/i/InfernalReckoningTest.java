package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BronzeSable;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class InfernalReckoningTest extends BaseCardTest {

    private void prepareCast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new InfernalReckoning()));
        harness.addMana(player1, ManaColor.BLACK, 1);
    }

    @Test
    @DisplayName("Exiles the colorless creature and gains life equal to its power")
    void exilesColorlessCreatureAndGainsLife() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player2, new BronzeSable());
        UUID targetId = harness.getPermanentId(player2, "Bronze Sable");

        prepareCast();
        harness.castInstant(player1, 0, targetId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bronze Sable");
        harness.assertNotInGraveyard(player2, "Bronze Sable");
        // Bronze Sable is a 2/1
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Life gain reads the power as modified before the exile")
    void lifeGainReadsModifiedPower() {
        harness.setLife(player1, 20);
        Permanent sable = harness.addToBattlefieldAndReturn(player2, new BronzeSable());
        sable.setPowerModifier(3);

        prepareCast();
        harness.castInstant(player1, 0, sable.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bronze Sable");
        assertThat(harness.getGameData().playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("Cannot target a colored creature")
    void cannotTargetColoredCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = harness.getPermanentId(player2, "Grizzly Bears");

        prepareCast();
        assertThatThrownBy(() -> harness.castInstant(player1, 0, targetId))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }
}
