package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrimPoppet;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlourishingDefenses.class, GrimPoppet.class})
class FlourishingDefensesTest extends BaseCardTest {

    /**
     * Drives the stack to completion, answering every Flourishing Defenses "you may create a token"
     * prompt with {@code accept}. Bounded so a stuck state fails fast instead of hanging.
     */
    private void resolveAllMayPrompts(Player controller, boolean accept) {
        for (int guard = 0; guard < 40; guard++) {
            if (gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class) != null) {
                harness.handleMayAbilityChosen(controller, accept);
            } else if (!gd.stack.isEmpty()) {
                harness.passBothPriorities();
            } else {
                break;
            }
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private long elfWarriorTokenCount(Player player) {
        return countPermanents(player, "Elf Warrior");
    }

    @Test
    @DisplayName("Triggers once per -1/-1 counter — three counters create three tokens")
    void createsTokenPerCounter() {
        harness.addToBattlefield(player1, new FlourishingDefenses());
        // Grim Poppet is a 4/4 that enters with three -1/-1 counters, so no death interferes.
        harness.enterBattlefieldAndReturn(player2, new GrimPoppet());
        resolveAllMayPrompts(player1, true);

        // Grim Poppet enters with 3 -1/-1 counters at once, so Flourishing Defenses triggers 3 times.
        // Tokens belong to the Flourishing Defenses controller, even though the counters landed
        // on an opponent's creature.
        assertThat(elfWarriorTokenCount(player1)).isEqualTo(3);
        assertThat(elfWarriorTokenCount(player2)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining the may-ability creates no token")
    void decliningCreatesNoToken() {
        harness.addToBattlefield(player1, new FlourishingDefenses());
        harness.enterBattlefieldAndReturn(player2, new GrimPoppet());
        resolveAllMayPrompts(player1, false);

        assertThat(elfWarriorTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("Opponent's Flourishing Defenses triggers for its own controller")
    void triggersForItsOwnController() {
        // Flourishing Defenses belongs to player2; player1's Grim Poppet enters with -1/-1
        // counters — the tokens are created by player2 (the Flourishing Defenses controller).
        harness.addToBattlefield(player2, new FlourishingDefenses());
        harness.enterBattlefieldAndReturn(player1, new GrimPoppet());
        resolveAllMayPrompts(player2, true);

        assertThat(elfWarriorTokenCount(player2)).isEqualTo(3);
        assertThat(elfWarriorTokenCount(player1)).isZero();
    }
}
