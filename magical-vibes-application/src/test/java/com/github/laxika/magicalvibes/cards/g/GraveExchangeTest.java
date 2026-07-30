package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GraveExchangeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns creature card from graveyard and target player sacrifices a creature")
    void returnsCreatureAndTargetPlayerSacrifices() {
        Card graveyardCreature = new GrizzlyBears();
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new GraveExchange()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(player2.getId()));
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(graveyardCreature.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        harness.assertInGraveyard(player1, "Grave Exchange");
    }

    @Test
    @DisplayName("Controller can be the target player and sacrifices their own creature")
    void canTargetSelf() {
        Card graveyardCreature = new GrizzlyBears();
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new GraveExchange()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(player1.getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("Target player with no creatures still returns the graveyard creature")
    void noCreaturesToSacrifice() {
        Card graveyardCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(graveyardCreature));
        harness.setHand(player1, List.of(new GraveExchange()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, graveyardCreature.getId(), List.of(player2.getId()));
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(graveyardCreature.getId()));
    }

    @Test
    @DisplayName("Cannot target a noncreature card in the graveyard")
    void cannotTargetNoncreatureCard() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new GraveExchange()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId(), List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast without a graveyard target")
    void cannotCastWithoutGraveyardTarget() {
        harness.setHand(player1, List.of(new GraveExchange()));
        harness.addMana(player1, ManaColor.BLACK, 6);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player2.getId())))
                .isInstanceOf(IllegalStateException.class);
    }
}
