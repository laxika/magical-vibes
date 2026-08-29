package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.c.ChandraBoldPyromancer;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ObliteratingBoltTest extends BaseCardTest {

    @Test
    @DisplayName("Kills a creature and exiles it instead of putting it into the graveyard")
    void killsAndExilesCreature() {
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent target = findPermanent(player2, "Grizzly Bears");
        harness.setHand(player1, List.of(new ObliteratingBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("Kills a planeswalker and exiles it instead of putting it into the graveyard")
    void killsAndExilesPlaneswalker() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        target.setCounterCount(CounterType.LOYALTY, 4);
        harness.setHand(player1, List.of(new ObliteratingBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        harness.assertNotOnBattlefield(player2, "Chandra, Bold Pyromancer");
        harness.assertNotInGraveyard(player2, "Chandra, Bold Pyromancer");
        assertThat(gameData.getPlayerExiledCards(player2.getId()))
                .extracting(card -> card.getName())
                .contains("Chandra, Bold Pyromancer");
    }

    @Test
    @DisplayName("Cannot target a noncreature nonplaneswalker permanent")
    void cannotTargetLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.setHand(player1, List.of(new ObliteratingBolt()));
        harness.addMana(player1, ManaColor.RED, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
