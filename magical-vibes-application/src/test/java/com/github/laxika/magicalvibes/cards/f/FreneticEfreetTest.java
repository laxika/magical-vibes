package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class FreneticEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("The {0} ability either phases the Efreet out (win) or sacrifices it (loss)")
    void flipResolvesToExactlyOneBranch() {
        Permanent efreet = addEfreet(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        boolean phasedOut = isPhasedOut(efreet);
        boolean inGraveyard = gd.playerGraveyards.get(player1.getId()).stream()
                .anyMatch(c -> c.getName().equals("Frenetic Efreet"));

        assertThat(phasedOut != inGraveyard)
                .as("Frenetic Efreet must be phased out (win) or in the graveyard (loss)")
                .isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(efreet);

        if (phasedOut) {
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                    .anyMatch(log -> log.contains("wins the coin flip"));
        } else {
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                    .anyMatch(log -> log.contains("loses the coin flip"));
        }
    }

    @Test
    @DisplayName("The ability costs {0} — no mana is needed to activate it")
    void abilityIsFree() {
        addEfreet(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains("coin flip for Frenetic Efreet"));
    }

    @Test
    @DisplayName("A phased-out Efreet phases back in during its controller's next untap step")
    void phasesBackIn() {
        Permanent efreet = addEfreet(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        if (!isPhasedOut(efreet)) {
            return; // Lost the flip: it was sacrificed, nothing to phase back in.
        }

        advanceTurn(); // player2's turn
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(efreet);

        advanceTurn(); // player1's untap step
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(efreet);
    }

    private boolean isPhasedOut(Permanent permanent) {
        return gd.phasedOutPermanents.getOrDefault(player1.getId(), java.util.List.of()).contains(permanent);
    }

    private void advanceTurn() {
        harness.forceStep(TurnStep.CLEANUP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    private Permanent addEfreet(Player player) {
        Permanent perm = new Permanent(new FreneticEfreet());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
