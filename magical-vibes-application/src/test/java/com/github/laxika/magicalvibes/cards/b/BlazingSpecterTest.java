package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.r.RazorfootGriffin;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BlazingSpecter.class, Forest.class, RazorfootGriffin.class})
class BlazingSpecterTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player makes that player discard a card")
    void combatDamageMakesDamagedPlayerDiscard() {
        addAttackingSpecter(player1);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new RazorfootGriffin())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player2, "Razorfoot Griffin");
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Combat damage to a player with no cards does not create a discard choice")
    void combatDamageWithEmptyHandDoesNotPrompt() {
        addAttackingSpecter(player1);
        harness.setHand(player2, List.of());
        int initialGraveyardSize = gd.playerGraveyards.get(player2.getId()).size();

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(initialGraveyardSize);
    }

    @Test
    @DisplayName("No trigger when Blazing Specter is blocked and deals no combat damage to a player")
    void noTriggerWhenBlocked() {
        addAttackingSpecter(player1);
        Permanent blocker = addCreatureReady(player2, new RazorfootGriffin());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));

        resolveCombatAndTrigger();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class)).isNull();
        harness.assertInHand(player2, "Forest");
    }

    private Permanent addAttackingSpecter(Player player) {
        Permanent specter = addCreatureReady(player, new BlazingSpecter());
        specter.setAttacking(true);
        return specter;
    }

    private void resolveCombatAndTrigger() {
        resolveCombat();
        harness.passBothPriorities();
    }
}
