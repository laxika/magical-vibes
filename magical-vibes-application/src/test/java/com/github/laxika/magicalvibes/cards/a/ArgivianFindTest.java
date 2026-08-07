package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ArgivianFindTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target artifact card from your graveyard to your hand")
    void returnsArtifactFromGraveyardToHand() {
        Card artifact = new Ornithopter();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new ArgivianFind()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(artifact.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(artifact.getId()));
    }

    @Test
    @DisplayName("Returns target enchantment card from your graveyard to your hand")
    void returnsEnchantmentFromGraveyardToHand() {
        Card enchantment = new Pacifism();
        harness.setGraveyard(player1, List.of(enchantment));
        harness.setHand(player1, List.of(new ArgivianFind()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castInstant(player1, 0, enchantment.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(enchantment.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(enchantment.getId()));
    }

    @Test
    @DisplayName("Cannot target a creature card")
    void cannotTargetCreatureCard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new ArgivianFind()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a card in an opponent's graveyard")
    void cannotTargetOpponentGraveyard() {
        Card artifact = new Ornithopter();
        harness.setGraveyard(player2, List.of(artifact));
        harness.setHand(player1, List.of(new ArgivianFind()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }
}
