package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Imperiosaur;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({StrongholdRats.class, Imperiosaur.class})
class StrongholdRatsTest extends BaseCardTest {

    @Test
    void discardsWaitUntilEveryPlayerHasChosen() {
        Imperiosaur firstDiscard = new Imperiosaur();
        Imperiosaur secondDiscard = new Imperiosaur();
        harness.setHand(player1, List.of(firstDiscard, new StrongholdRats()));
        harness.setHand(player2, List.of(new StrongholdRats(), secondDiscard));
        Permanent rats = addCreatureReady(player1, new StrongholdRats());
        rats.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(firstDiscard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(secondDiscard);
    }

    @Test
    void creatureWithoutShadowCannotBlockRats() {
        Permanent rats = addCreatureReady(player1, new StrongholdRats());
        rats.setAttacking(true);
        addCreatureReady(player2, new Imperiosaur());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    void ratsCannotBlockCreatureWithoutShadow() {
        Permanent attacker = addCreatureReady(player1, new Imperiosaur());
        attacker.setAttacking(true);
        addCreatureReady(player2, new StrongholdRats());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shadow");
    }

    @Test
    void combatDamageToShadowBlockerDoesNotCauseDiscard() {
        harness.setHand(player1, List.of(new Imperiosaur()));
        harness.setHand(player2, List.of(new Imperiosaur()));
        Permanent rats = addCreatureReady(player1, new StrongholdRats());
        rats.setAttacking(true);
        addCreatureReady(player2, new StrongholdRats());
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
    }

    @Test
    void combatDamageMakesEachPlayerDiscard() {
        harness.setHand(player1, List.of(new Imperiosaur()));
        harness.setHand(player2, List.of(new Imperiosaur()));

        Permanent rats = addCreatureReady(player1, new StrongholdRats());
        rats.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    void combatDamageSkipsPlayersWithEmptyHands() {
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Imperiosaur()));

        Permanent rats = addCreatureReady(player1, new StrongholdRats());
        rats.setAttacking(true);

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }
}
