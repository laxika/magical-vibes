package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Relearn.class, Counterspell.class, RaiseDead.class, GrizzlyBears.class})
class RelearnTest extends BaseCardTest {

    @Test
    @DisplayName("Relearn returns target instant from graveyard to hand")
    void returnsTargetInstantFromGraveyardToHand() {
        Card instant = new Counterspell();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new Relearn()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, instant.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(instant.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(instant.getId()));
    }

    @Test
    @DisplayName("Relearn returns target sorcery from graveyard to hand")
    void returnsTargetSorceryFromGraveyardToHand() {
        Card sorcery = new RaiseDead();
        harness.setGraveyard(player1, List.of(sorcery));
        harness.setHand(player1, List.of(new Relearn()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, sorcery.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).anyMatch(c -> c.getId().equals(sorcery.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(c -> c.getId().equals(sorcery.getId()));
    }

    @Test
    @DisplayName("Relearn cannot target creature card in graveyard")
    void cannotTargetCreatureCardInGraveyard() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new Relearn()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Relearn cannot target card in opponent's graveyard")
    void cannotTargetCardInOpponentGraveyard() {
        Card instant = new Counterspell();
        harness.setGraveyard(player2, List.of(instant));
        harness.setHand(player1, List.of(new Relearn()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("your graveyard");
    }

    @Test
    @DisplayName("Relearn cannot be cast without a graveyard target")
    void cannotCastWithoutGraveyardTarget() {
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new Relearn()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, (UUID) null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Relearn fizzles if the targeted card leaves the graveyard before resolution")
    void fizzlesIfTargetLeavesGraveyardBeforeResolution() {
        Card instant = new Counterspell();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new Relearn()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castSorcery(player1, 0, instant.getId());
        harness.getGameData().playerGraveyards.get(player1.getId()).clear();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.plainText().contains("fizzles"));
    }
}
