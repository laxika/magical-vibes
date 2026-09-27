package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HideousLaughter;
import com.github.laxika.magicalvibes.cards.w.WanderingOnes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SoullessRevival.class, HideousLaughter.class, WanderingOnes.class})
class SoullessRevivalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature card from graveyard to hand")
    void returnsCreatureFromGraveyardToHand() {
        Card creature = new WanderingOnes();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new SoullessRevival()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Wandering Ones");
        harness.assertNotInGraveyard(player1, "Wandering Ones");
        harness.assertInGraveyard(player1, "Soulless Revival");
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotTargetNonCreature() {
        Card instant = new HideousLaughter();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new SoullessRevival()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature card in an opponent's graveyard")
    void cannotTargetOpponentsCreature() {
        Card creature = new WanderingOnes();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new SoullessRevival()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Card creature = new WanderingOnes();
        SoullessRevival revival = new SoullessRevival();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HideousLaughter(), revival));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castWithSplice(player1, 0, creature.getId(), List.of(1));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Soulless Revival");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
    }
}
