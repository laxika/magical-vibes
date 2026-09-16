package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningSkelemental.class, GrizzlyBears.class, Forest.class})
class LightningSkelementalTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage makes the damaged player discard two cards")
    void combatDamageMakesDamagedPlayerDiscardTwoCards() {
        Permanent skelemental = addAttackingSkelemental(player1);
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(skelemental);
    }

    @Test
    @DisplayName("Lightning Skelemental is sacrificed at the end step")
    void sacrificedAtEndStep() {
        harness.addToBattlefieldAndReturn(player1, new LightningSkelemental());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Lightning Skelemental");
        harness.assertInGraveyard(player1, "Lightning Skelemental");
    }

    private Permanent addAttackingSkelemental(Player player) {
        Permanent skelemental = addCreatureReady(player, new LightningSkelemental());
        skelemental.setAttacking(true);
        return skelemental;
    }
}
