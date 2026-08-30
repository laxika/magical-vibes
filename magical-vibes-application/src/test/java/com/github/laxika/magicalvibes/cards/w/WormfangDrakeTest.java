package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WormfangDrake.class, GrizzlyBears.class, Unsummon.class})
class WormfangDrakeTest extends BaseCardTest {

    private void castWormfangDrake() {
        harness.setHand(player1, List.of(new WormfangDrake()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Sacrifices itself when its controller has no other creature")
    void sacrificesItselfWithoutAnotherCreature() {
        castWormfangDrake();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wormfang Drake");
        harness.assertInGraveyard(player1, "Wormfang Drake");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Exiles another creature it controls")
    void exilesAnotherCreature() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castWormfangDrake();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        harness.assertOnBattlefield(player1, "Wormfang Drake");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Returns the exiled creature when it leaves the battlefield")
    void returnsExiledCreatureWhenItLeaves() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        castWormfangDrake();
        harness.passBothPriorities();

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bearsId);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        UUID drakeId = harness.getPermanentId(player1, "Wormfang Drake");
        harness.castInstant(player1, 0, drakeId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Wormfang Drake");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(card -> card.getName().equals("Grizzly Bears"));
    }
}
