package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.s.Skinrender;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

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
                return;
            }
        }
    }

    private long elfWarriorTokenCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Elf Warrior"))
                .count();
    }

    @Test
    @DisplayName("Triggers once per -1/-1 counter — three counters create three tokens")
    void createsTokenPerCounter() {
        harness.addToBattlefield(player1, new FlourishingDefenses());
        // 4/4 survives three -1/-1 counters (becomes 1/1), so no death interferes.
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
        resolveAllMayPrompts(player1, true);

        // Skinrender puts 3 -1/-1 counters at once → Flourishing Defenses triggers 3 times.
        // Tokens belong to the Flourishing Defenses controller, even though the counters landed
        // on an opponent's creature.
        assertThat(elfWarriorTokenCount(player1)).isEqualTo(3);
        assertThat(elfWarriorTokenCount(player2)).isEqualTo(0);
    }

    @Test
    @DisplayName("Declining the may-ability creates no token")
    void decliningCreatesNoToken() {
        harness.addToBattlefield(player1, new FlourishingDefenses());
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
        resolveAllMayPrompts(player1, false);

        assertThat(elfWarriorTokenCount(player1)).isZero();
    }

    @Test
    @DisplayName("Opponent's Flourishing Defenses triggers for its own controller")
    void triggersForItsOwnController() {
        // Flourishing Defenses belongs to player2; player1's Skinrender puts -1/-1 counters on a
        // creature — the tokens are created by player2 (the Flourishing Defenses controller).
        harness.addToBattlefield(player2, new FlourishingDefenses());
        harness.addToBattlefield(player2, new AirElemental());
        UUID targetId = harness.getPermanentId(player2, "Air Elemental");

        harness.setHand(player1, List.of(new Skinrender()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.getGameService().playCard(gd, player1, 0, 0, targetId, null);
        resolveAllMayPrompts(player2, true);

        assertThat(elfWarriorTokenCount(player2)).isEqualTo(3);
        assertThat(elfWarriorTokenCount(player1)).isZero();
    }
}
