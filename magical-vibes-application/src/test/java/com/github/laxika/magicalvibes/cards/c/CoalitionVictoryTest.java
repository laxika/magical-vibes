package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CoalitionVictoryTest extends BaseCardTest {

    @Test
    @DisplayName("Wins with a land of each basic type and a creature of each color")
    void winsWithAllRequiredLandsAndColors() {
        addAllBasicLandTypes(player1);
        harness.addToBattlefield(player1, fiveColorCreature());
        castCoalitionVictory();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not win when one required basic land type is missing")
    void doesNotWinWithoutEachBasicLandType() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player1, fiveColorCreature());
        castCoalitionVictory();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not win when one required creature color is missing")
    void doesNotWinWithoutEachCreatureColor() {
        addAllBasicLandTypes(player1);
        harness.addToBattlefield(player1, creatureOfColors(
                "Four-color creature", CardColor.WHITE, CardColor.BLUE, CardColor.BLACK, CardColor.RED));
        castCoalitionVictory();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Opponent-controlled lands and creatures do not satisfy the condition")
    void opponentPermanentsDoNotCount() {
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Mountain());
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, fiveColorCreature());
        castCoalitionVictory();

        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    private void castCoalitionVictory() {
        harness.setHand(player1, List.of(new CoalitionVictory()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void addAllBasicLandTypes(Player player) {
        harness.addToBattlefield(player, new Plains());
        harness.addToBattlefield(player, new Island());
        harness.addToBattlefield(player, new Swamp());
        harness.addToBattlefield(player, new Mountain());
        harness.addToBattlefield(player, new Forest());
    }

    private Card fiveColorCreature() {
        return creatureOfColors("Five-color creature", CardColor.WHITE, CardColor.BLUE,
                CardColor.BLACK, CardColor.RED, CardColor.GREEN);
    }

    private Card creatureOfColors(String name, CardColor... colors) {
        Card creature = new Card();
        creature.setName(name);
        creature.setType(CardType.CREATURE);
        creature.setPower(1);
        creature.setToughness(1);
        creature.setColor(colors[0]);
        creature.setColors(List.of(colors));
        return creature;
    }
}
