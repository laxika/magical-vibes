package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DimirCutpurse.class, Forest.class, GrizzlyBears.class})
class DimirCutpurseTest extends BaseCardTest {

    @Test
    void combatDamageMakesPlayerDiscardAndControllerDraw() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new GrizzlyBears(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        addAttackingCutpurse(player1);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyHandStillAllowsControllerToDraw() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        addAttackingCutpurse(player1);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void blockedCutpurseDoesNotTrigger() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of(new Forest()));

        addAttackingCutpurse(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent addAttackingCutpurse(com.github.laxika.magicalvibes.model.Player player) {
        gd.playerAutoStopSteps.put(player1.getId(), Set.of(TurnStep.END_OF_COMBAT));
        gd.playerAutoStopSteps.put(player2.getId(), Set.of(TurnStep.END_OF_COMBAT));
        Permanent cutpurse = addCreatureReady(player, new DimirCutpurse());
        cutpurse.setAttacking(true);
        return cutpurse;
    }
}
