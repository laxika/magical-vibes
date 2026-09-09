package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MindRot;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BearCub.class, BreathOfLife.class, GrizzlyBears.class, MindRot.class})
class BreathOfLifeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature card from your graveyard to the battlefield")
    void returnsCreatureFromGraveyardToBattlefield() {
        Card creature = new BearCub();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BreathOfLife()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Cannot target non-creature card in graveyard")
    void cannotTargetNonCreatureCard() {
        Card nonCreatureCard = new MindRot();
        harness.setGraveyard(player1, List.of(nonCreatureCard));
        harness.setHand(player1, List.of(new BreathOfLife()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, nonCreatureCard.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target card in opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card creature = new BearCub();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new BreathOfLife()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Fizzles if target creature leaves graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyard() {
        Card creature = new BearCub();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BreathOfLife()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creature.getId()));
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("fizzles"));
    }

    @Test
    @DisplayName("Returns only the selected creature card when multiple creatures are in your graveyard")
    void returnsOnlySelectedCreatureFromMultipleGraveyardCreatures() {
        Card otherCreature = new GrizzlyBears();
        Card selectedCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(otherCreature, selectedCreature));
        harness.setHand(player1, List.of(new BreathOfLife()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveSorcery(player1, 0, selectedCreature.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(selectedCreature.getId()))
                .noneMatch(p -> p.getCard().getId().equals(otherCreature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(otherCreature.getId()))
                .noneMatch(c -> c.getId().equals(selectedCreature.getId()));
    }
}
