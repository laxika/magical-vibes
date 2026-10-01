package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.CreakwoodGhoul;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NeedleSpecter.class, CreakwoodGhoul.class})
class NeedleSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player makes that player discard cards equal to the damage dealt")
    void discardsCardsEqualToCombatDamage() {
        // Two +1/+1 counters make the 1/1 Specter deal 3 combat damage.
        Permanent specter = addAttackingSpecter(player1);
        specter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player2, List.of(new CreakwoodGhoul(), new CreakwoodGhoul(), new CreakwoodGhoul()));

        resolveCombatAndTrigger();

        // Damaged player must discard exactly three cards, one choice at a time.
        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
            assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                    .isEqualTo(player2.getId());
            harness.handleCardChosen(player2, 0);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Discards only as many as it can when the player has fewer cards than the damage")
    void discardsLimitedByHandSize() {
        Permanent specter = addAttackingSpecter(player1);
        specter.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2); // deals 3
        harness.setHand(player2, List.of(new CreakwoodGhoul()));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("No trigger when the Specter is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingSpecter(player1);
        Permanent blocker = addCreatureReady(player2, new CreakwoodGhoul());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, List.of(new CreakwoodGhoul()));

        resolveCombatAndTrigger();

        // No combat damage reached the player, so no discard was prompted.
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
    }

    // ===== Helpers =====

    private Permanent addAttackingSpecter(Player player) {
        Permanent specter = addCreatureReady(player, new NeedleSpecter());
        specter.setAttacking(true);
        return specter;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        resolveAllTriggers();
    }
}
