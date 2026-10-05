package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.l.LotusPathDjinn;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindscourDragon.class, LotusPathDjinn.class})
class MindscourDragonTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage lets the controller choose any player to mill four cards")
    void combatDamageMillsTargetPlayer() {
        Permanent dragon = addCreatureReady(player1, new MindscourDragon());
        dragon.setAttacking(true);
        dragon.setAttackTarget(player2.getId());

        harness.setLibrary(player1, List.of(new LotusPathDjinn(), new LotusPathDjinn(), new LotusPathDjinn(), new LotusPathDjinn()));
        harness.setLibrary(player2, List.of(new LotusPathDjinn(), new LotusPathDjinn(), new LotusPathDjinn(), new LotusPathDjinn()));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Mindscour Dragon does not trigger when blocked")
    void blockedDragonDoesNotTrigger() {
        Permanent dragon = addCreatureReady(player1, new MindscourDragon());
        dragon.setAttacking(true);
        dragon.setAttackTarget(player2.getId());
        Permanent blocker = addCreatureReady(player2, new LotusPathDjinn());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.setLibrary(player1, List.of(new LotusPathDjinn(), new LotusPathDjinn(), new LotusPathDjinn(), new LotusPathDjinn()));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage mills exactly four cards from the chosen opponent")
    void combatDamageMillsOpponent() {
        Permanent dragon = addCreatureReady(player1, new MindscourDragon());
        dragon.setAttacking(true);
        dragon.setAttackTarget(player2.getId());
        LotusPathDjinn remaining = new LotusPathDjinn();
        harness.setLibrary(player2, List.of(new LotusPathDjinn(), new LotusPathDjinn(),
                new LotusPathDjinn(), new LotusPathDjinn(), remaining));

        resolveCombat();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(4);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A target with fewer than four cards mills its remaining library")
    void shortLibraryMillsOnlyRemainingCards() {
        Permanent dragon = addCreatureReady(player1, new MindscourDragon());
        dragon.setAttacking(true);
        dragon.setAttackTarget(player2.getId());
        harness.setLibrary(player2, List.of(new LotusPathDjinn(), new LotusPathDjinn()));

        resolveCombat();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
    }
}
