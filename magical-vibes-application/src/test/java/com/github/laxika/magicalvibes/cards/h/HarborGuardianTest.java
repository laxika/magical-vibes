package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HarborGuardian.class, Forest.class})
class HarborGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("When it attacks, the defending player may draw a card (accept)")
    void defendingPlayerDraws() {
        addCreatureReady(player1, new HarborGuardian());
        harness.setLibrary(player2, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player2.getId());

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore + 1);
    }

    @Test
    @DisplayName("Defending player may decline the draw")
    void defendingPlayerDeclines() {
        addCreatureReady(player1, new HarborGuardian());
        harness.setLibrary(player2, List.of(new Forest()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player2.getId()).size();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.playerHands.get(player2.getId()).size()).isEqualTo(handBefore);
    }

    @Test
    @DisplayName("When the other player attacks, the defending player receives the choice")
    void otherPlayerAttacks() {
        addCreatureReady(player2, new HarborGuardian());
        harness.setLibrary(player1, List.of(new Forest()));

        declareAttackers(player2, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore + 1);
    }

}
