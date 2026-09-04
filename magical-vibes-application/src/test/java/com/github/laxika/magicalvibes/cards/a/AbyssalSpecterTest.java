package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.k.KjeldoranSkyknight;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbyssalSpecter.class, Counterspell.class, Island.class, KjeldoranSkyknight.class})
class AbyssalSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player makes that player discard a card of their choice")
    void combatDamageMakesDamagedPlayerDiscard() {
        addAttackingSpecter(player1);
        harness.setHand(player2, List.of(new Counterspell(), new Island()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No trigger when the Specter is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingSpecter(player1);
        addCreatureReady(player2, new KjeldoranSkyknight());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        resolveAllTriggers();

        // No combat damage reached the player, so no discard was prompted.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    @Test
    @DisplayName("A damaged player with an empty hand has no card to discard")
    void emptyHandDoesNotCreateDiscardChoice() {
        addAttackingSpecter(player1);
        harness.setHand(player2, List.of());
        int graveyardSizeBefore = gd.playerGraveyards.get(player2.getId()).size();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(graveyardSizeBefore);
    }

    @Test
    @DisplayName("Noncombat damage to a player also makes that player discard a card")
    void noncombatDamageMakesDamagedPlayerDiscard() {
        AbyssalSpecter card = new AbyssalSpecter();
        card.addActivatedAbility(new ActivatedAbility(true, null,
                List.of(new DealDamageToAnyTargetEffect(1)), "{T}: This creature deals 1 damage to any target."));
        Permanent specter = addCreatureReady(player1, card);
        harness.setHand(player2, List.of(new Counterspell()));

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(specter),
                null, player2.getId());
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNotNull();
    }

    private Permanent addAttackingSpecter(Player player) {
        Permanent specter = addCreatureReady(player, new AbyssalSpecter());
        specter.setAttacking(true);
        return specter;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities(); // resolve what combat damage triggered
    }
}
