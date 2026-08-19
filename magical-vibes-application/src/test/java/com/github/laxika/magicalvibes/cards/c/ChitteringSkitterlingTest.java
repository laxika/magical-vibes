package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChitteringSkitterlingTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing itself draws a card when an opponent has three poison counters")
    void sacrificesItselfAndDrawsCard() {
        Permanent skitterling = addCreatureReady(player1, new ChitteringSkitterling());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        gd.playerPoisonCounters.put(player2.getId(), 3);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Chittering Skitterling");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(skitterling);
    }

    @Test
    @DisplayName("Can sacrifice an artifact instead of itself and draw a card")
    void sacrificesArtifactAndDrawsCard() {
        Permanent skitterling = addCreatureReady(player1, new ChitteringSkitterling());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skitterling);
        harness.assertInGraveyard(player1, "Spellbook");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Requires an opponent to have three poison counters")
    void requiresOpponentPoisonThreshold() {
        Permanent skitterling = addCreatureReady(player1, new ChitteringSkitterling());
        gd.playerPoisonCounters.put(player1.getId(), 3);
        gd.playerPoisonCounters.put(player2.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skitterling);
    }

    @Test
    @DisplayName("Cannot be activated more than once each turn")
    void onlyOnceEachTurn() {
        Permanent skitterling = addCreatureReady(player1, new ChitteringSkitterling());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new Spellbook());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skitterling);
    }
}
