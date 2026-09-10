package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.k.KnightOfDawn;
import com.github.laxika.magicalvibes.cards.s.SoltariFootSoldier;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThalakosSeer.class, KnightOfDawn.class, SoltariFootSoldier.class})
class ThalakosSeerTest extends BaseCardTest {

    @Test
    @DisplayName("Leaving the battlefield draws a card")
    void leavingDrawsACard() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new ThalakosSeer());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, seer));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Leaving the battlefield by exile draws a card")
    void exilingDrawsACard() {
        Permanent seer = harness.addToBattlefieldAndReturn(player1, new ThalakosSeer());

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToExile(gd, seer));
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Shadow prevents a non-shadow creature from blocking Thalakos Seer")
    void shadowPreventsNonShadowBlocker() {
        Permanent seer = addCreatureReady(player1, new ThalakosSeer());
        seer.setAttacking(true);
        addCreatureReady(player2, new KnightOfDawn());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Shadow allows a shadow creature to block Thalakos Seer")
    void shadowAllowsShadowBlocker() {
        Permanent seer = addCreatureReady(player1, new ThalakosSeer());
        seer.setAttacking(true);
        Permanent shadowBlocker = addCreatureReady(player2, new SoltariFootSoldier());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(shadowBlocker.isBlocking()).isTrue();
    }
}
