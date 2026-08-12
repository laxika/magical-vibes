package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TemurAscendancyTest extends BaseCardTest {

    @Test
    @DisplayName("Grants haste to creatures its controller controls")
    void grantsHasteToOwnCreaturesOnly() {
        harness.addToBattlefield(player1, new TemurAscendancy());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, findPermanent(player1, "Grizzly Bears"), Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, findPermanent(player2, "Grizzly Bears"), Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("May draw when a creature with power 4 or greater enters under its controller's control")
    void mayDrawForHighPowerCreature() {
        harness.addToBattlefield(player1, new TemurAscendancy());
        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Does not draw for a creature with power less than 4")
    void doesNotTriggerForLowPowerCreature() {
        harness.addToBattlefield(player1, new TemurAscendancy());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger for an opponent's creature")
    void doesNotTriggerForOpponentCreature() {
        harness.addToBattlefield(player1, new TemurAscendancy());
        harness.setHand(player1, List.of());
        harness.addToBattlefield(player2, new AirElemental());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
