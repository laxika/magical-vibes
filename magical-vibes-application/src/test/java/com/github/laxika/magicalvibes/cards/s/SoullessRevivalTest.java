package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HideousLaughter;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoullessRevivalTest extends BaseCardTest {

    @Test
    @DisplayName("Returns target creature card from graveyard to hand")
    void returnsCreatureFromGraveyardToHand() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new SoullessRevival()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()))
                .anyMatch(c -> c.getName().equals("Soulless Revival"));
    }

    @Test
    @DisplayName("Cannot target a non-creature card in the graveyard")
    void cannotTargetNonCreature() {
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new SoullessRevival()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Splices onto an Arcane spell and stays in hand")
    void splicesOntoArcaneSpell() {
        Card creature = new GrizzlyBears();
        SoullessRevival revival = new SoullessRevival();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new HideousLaughter(), revival));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castWithSplice(player1, 0, creature.getId(), List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(revival);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
    }
}
