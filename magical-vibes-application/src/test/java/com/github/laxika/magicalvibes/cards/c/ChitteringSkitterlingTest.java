package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.p.PropheticPrism;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChitteringSkitterling.class, PropheticPrism.class})
class ChitteringSkitterlingTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing itself draws a card when an opponent has three poison counters")
    void sacrificesItselfAndDrawsCard() {
        Permanent skitterling = addCreatureReady(player1, new ChitteringSkitterling());
        harness.setHand(player1, List.of(new ChitteringSkitterling()));
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        gd.playerPoisonCounters.put(player2.getId(), 3);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skitterling);
        harness.assertInGraveyard(player1, "Prophetic Prism");
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
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        harness.clearPriorityPassed();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skitterling);
    }

    @Test
    @DisplayName("Can sacrifice another creature while tapped and summoning sick")
    void sacrificesAnotherCreatureWhileTappedAndSummoningSick() {
        Permanent skitterling = harness.addToBattlefieldAndReturn(player1, new ChitteringSkitterling());
        skitterling.setSummoningSick(true);
        skitterling.setTapped(true);
        Permanent sacrifice = harness.addToBattlefieldAndReturn(player1, new ChitteringSkitterling());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new PropheticPrism()));
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, sacrifice.getId());

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skitterling).doesNotContain(sacrifice);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();

        harness.assertInHand(player1, "Prophetic Prism");
        harness.assertInGraveyard(player1, "Chittering Skitterling");
        assertThat(skitterling.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Losing corrupted after activation does not prevent drawing")
    void drawsEvenIfOpponentPoisonDropsBeforeResolution() {
        harness.addToBattlefield(player1, new ChitteringSkitterling());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new PropheticPrism()));
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.assertInGraveyard(player1, "Chittering Skitterling");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gd.playerPoisonCounters.put(player2.getId(), 2);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Prophetic Prism");
    }

    @Test
    @DisplayName("Each copy may activate once during the same turn")
    void activationLimitIsPerPermanent() {
        Permanent first = addCreatureReady(player1, new ChitteringSkitterling());
        Permanent second = addCreatureReady(player1, new ChitteringSkitterling());
        Permanent artifact = harness.addToBattlefieldAndReturn(player1, new PropheticPrism());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new PropheticPrism(), new PropheticPrism()));
        gd.playerPoisonCounters.put(player2.getId(), 3);

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 1, null, null);
        harness.handlePermanentChosen(player1, second.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(first).doesNotContain(second);
    }
}
