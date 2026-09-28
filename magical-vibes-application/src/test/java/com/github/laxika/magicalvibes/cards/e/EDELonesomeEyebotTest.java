package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EDELonesomeEyebot.class, GrizzlyBears.class})
class EDELonesomeEyebotTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with more creatures than quest counters adds a quest counter")
    void attackAddsQuestCounterWhenAttackerCountIsGreater() {
        Permanent edE = addReadyEdE(player1);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(edE.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("The attack trigger does not add a quest counter when the counts are equal")
    void attackDoesNotAddQuestCounterWhenCountsAreEqual() {
        Permanent edE = addReadyEdE(player1);
        edE.setCounterCount(CounterType.QUEST, 1);
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(edE.getCounterCount(CounterType.QUEST)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sacrificing ED-E draws one card plus one for each quest counter")
    void sacrificeDrawsBaseCardAndQuestCounterCards() {
        Permanent edE = addReadyEdE(player1);
        edE.setCounterCount(CounterType.QUEST, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        assertThat(gameData.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        harness.assertNotOnBattlefield(player1, "ED-E, Lonesome Eyebot");
        harness.assertInGraveyard(player1, "ED-E, Lonesome Eyebot");
    }

    private Permanent addReadyEdE(Player player) {
        return addCreatureReady(player, new EDELonesomeEyebot());
    }
}
