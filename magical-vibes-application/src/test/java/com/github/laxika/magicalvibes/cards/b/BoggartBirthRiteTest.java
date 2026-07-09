package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SqueeGoblinNabob;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BoggartBirthRiteTest extends BaseCardTest {

    @Test
    @DisplayName("Boggart Birth Rite returns target Goblin card from graveyard to hand")
    void returnsGoblinFromGraveyardToHand() {
        Card goblin = new SqueeGoblinNabob();
        harness.setGraveyard(player1, List.of(goblin));
        harness.setHand(player1, List.of(new BoggartBirthRite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castSorcery(player1, 0, goblin.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(goblin.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(goblin.getId()));
    }

    @Test
    @DisplayName("Boggart Birth Rite cannot target non-Goblin card in graveyard")
    void cannotTargetNonGoblinCard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BoggartBirthRite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Boggart Birth Rite cannot target card in opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card goblin = new SqueeGoblinNabob();
        harness.setGraveyard(player2, List.of(goblin));
        harness.setHand(player1, List.of(new BoggartBirthRite()));
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, goblin.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }
}
