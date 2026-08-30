package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.d.Dodecapod;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Forget.class, GrizzlyBears.class, Island.class, Dodecapod.class})
class ForgetTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Forget targets a player")
    void castingTargetsPlayer() {
        harness.setHand(player1, List.of(new Forget()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getTargetId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Target discards two chosen cards, then draws two")
    void discardsTwoThenDrawsTwo() {
        GrizzlyBears discardedFirst = new GrizzlyBears();
        GrizzlyBears discardedSecond = new GrizzlyBears();
        GrizzlyBears retained = new GrizzlyBears();
        Island islandFirst = new Island();
        Island islandSecond = new Island();
        harness.setHand(player2, new ArrayList<>(List.of(discardedFirst, discardedSecond, retained)));
        harness.setLibrary(player2, new ArrayList<>(List.of(islandFirst, islandSecond)));
        harness.setHand(player1, List.of(new Forget()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Target player (not the caster) chooses which two to discard.
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0); // discard the first card
        harness.handleCardChosen(player2, 0); // discard the second card

        // Discarded two, then drew two — hand is back to three, both Islands drawn.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId()))
                .containsExactlyInAnyOrder(retained, islandFirst, islandSecond);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .contains(discardedFirst, discardedSecond);
    }

    @Test
    @DisplayName("Target holding one card discards it and draws only one")
    void oneCardDiscardsOneDrawsOne() {
        GrizzlyBears discarded = new GrizzlyBears();
        Island drawn = new Island();
        Island remaining = new Island();
        harness.setHand(player2, new ArrayList<>(List.of(discarded)));
        harness.setLibrary(player2, new ArrayList<>(List.of(drawn, remaining)));
        harness.setHand(player1, List.of(new Forget()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.handleCardChosen(player2, 0); // discard the only card

        // Discarded one, so draws only one (not two).
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(drawn);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(discarded);
    }

    @Test
    @DisplayName("Target with empty hand discards and draws nothing")
    void emptyHandDrawsNothing() {
        harness.setHand(player2, new ArrayList<>(List.of()));
        harness.setLibrary(player2, new ArrayList<>(List.of(new Island(), new Island())));
        harness.setHand(player1, List.of(new Forget()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, player2.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Targeting yourself does not make the discard opponent-caused")
    void selfTargetDoesNotEnableOpponentDiscardReplacement() {
        Forget forget = new Forget();
        Dodecapod dodecapod = new Dodecapod();
        GrizzlyBears grizzlyBears = new GrizzlyBears();
        Island islandFirst = new Island();
        Island islandSecond = new Island();
        harness.setHand(player1, new ArrayList<>(List.of(forget, dodecapod, grizzlyBears)));
        harness.setLibrary(player1, new ArrayList<>(List.of(islandFirst, islandSecond)));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0); // discard Dodecapod
        harness.handleCardChosen(player1, 0); // discard Grizzly Bears

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard() == dodecapod);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .contains(dodecapod, grizzlyBears);
        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(islandFirst, islandSecond);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
