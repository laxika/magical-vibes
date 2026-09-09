package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Peek;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RenderSpeechlessTest extends BaseCardTest {

    @Test
    void opponentDiscardsChosenNonlandAndTargetCreatureGetsTwoCounters() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new Peek())));
        harness.setHand(player1, List.of(new RenderSpeechless()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.RevealedHandChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.RevealedHandChoice.class).validIndices())
                .containsExactly(1);

        harness.handleCardChosen(player1, 1);

        harness.assertInGraveyard(player2, "Peek");
        assertThat(gd.playerHands.get(player2.getId())).extracting(card -> card.getName())
                .containsExactly("Forest");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void creatureTargetIsOptional() {
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));
        harness.setHand(player1, List.of(new RenderSpeechless()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player2, "Peek");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void cannotTargetNoncreatureForCounters() {
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player2, new ArrayList<>(List.of(new Peek())));
        harness.setHand(player1, List.of(new RenderSpeechless()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0,
                List.of(player2.getId(), harness.getPermanentId(player1, "Forest"))))
                .isInstanceOf(IllegalStateException.class);
    }
}
