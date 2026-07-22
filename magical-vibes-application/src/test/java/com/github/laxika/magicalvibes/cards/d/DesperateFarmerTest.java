package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.c.CruelEdict;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DesperateFarmerTest extends BaseCardTest {

    @Test
    @DisplayName("Transforms when another creature you control dies")
    void transformsWhenAllyCreatureDies() {
        harness.addToBattlefield(player1, new DesperateFarmer());
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent farmer = findPermanent(player1, "Desperate Farmer");

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities(); // resolve Cruel Edict → choose sacrifice

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        harness.handlePermanentChosen(player1, bears.getId());

        harness.passBothPriorities(); // resolve transform trigger

        assertThat(farmer.isTransformed()).isTrue();
        assertThat(farmer.getCard().getName()).isEqualTo("Depraved Harvester");
        assertThat(gqs.getEffectivePower(gd, farmer)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, farmer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when opponent's creature dies")
    void doesNotTriggerWhenOpponentCreatureDies() {
        harness.addToBattlefield(player1, new DesperateFarmer());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new CruelEdict()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        Permanent farmer = findPermanent(player1, "Desperate Farmer");
        assertThat(farmer.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Does not trigger when Desperate Farmer itself dies")
    void doesNotTriggerWhenSelfDies() {
        harness.addToBattlefield(player1, new DesperateFarmer());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new CruelEdict()));
        harness.addMana(player2, ManaColor.BLACK, 2);

        harness.castSorcery(player2, 0, player1.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Desperate Farmer"));
    }
}
