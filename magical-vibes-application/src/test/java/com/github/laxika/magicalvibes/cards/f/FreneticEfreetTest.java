package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EdgarKingOfFigaro;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FreneticEfreet.class, EdgarKingOfFigaro.class})
class FreneticEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("The {0} ability either phases the Efreet out (win) or sacrifices it (loss)")
    void flipResolvesToExactlyOneBranch() {
        Permanent efreet = addCreatureReady(player1, new FreneticEfreet());

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
            assertThat(gameLogContains("wins the coin flip")).isTrue();
        } else {
            assertThat(gameLogContains("loses the coin flip")).isTrue();
        }
    }

    @Test
    @DisplayName("The ability costs {0} — no mana is needed to activate it")
    void abilityIsFree() {
        addCreatureReady(player1, new FreneticEfreet());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gameLogContains("coin flip for Frenetic Efreet")).isTrue();
    }

    @Test
    @CardUsed(EdgarKingOfFigaro.class)
    @DisplayName("A phased-out Efreet phases back in during its controller's next untap step")
    void phasesBackIn() {
        Permanent efreet = addCreatureReady(player1, new FreneticEfreet());
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(isPhasedOut(efreet)).isTrue();

        harness.setHand(player2, List.of());
        harness.passUntil(player2, TurnStep.CLEANUP);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(efreet);

        harness.passUntil(player1, TurnStep.UNTAP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(efreet);
    }

    @Test
    @DisplayName("Stacked activations each flip a coin even after the Efreet is gone")
    void stackedActivationsStillFlipCoins() {
        Permanent efreet = addCreatureReady(player1, new FreneticEfreet());

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(3);
        resolveAllTriggers();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Frenetic Efreet"))).hasSize(3);
        boolean inGraveyard = gd.playerGraveyards.get(player1.getId()).contains(efreet.getCard());
        assertThat(isPhasedOut(efreet) != inGraveyard).isTrue();
        harness.assertNotOnBattlefield(player1, "Frenetic Efreet");
    }

    @Test
    @CardUsed(EdgarKingOfFigaro.class)
    @DisplayName("Remaining activations cannot sacrifice an Efreet that already phased out")
    void stackedActivationsLeavePhasedOutEfreetAlone() {
        Permanent efreet = addCreatureReady(player1, new FreneticEfreet());
        harness.addToBattlefield(player1, new EdgarKingOfFigaro());

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Frenetic Efreet"))).hasSize(2);
        assertThat(isPhasedOut(efreet)).isTrue();
        harness.assertNotInGraveyard(player1, "Frenetic Efreet");
    }

    @Test
    @DisplayName("A tapped, summoning-sick Efreet may activate its ability")
    void canActivateWhileTappedAndSummoningSick() {
        Permanent efreet = harness.addToBattlefieldAndReturn(player1, new FreneticEfreet());
        efreet.setSummoningSick(true);
        efreet.setTapped(true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gameLogContains("coin flip for Frenetic Efreet")).isTrue();
        boolean inGraveyard = gd.playerGraveyards.get(player1.getId()).contains(efreet.getCard());
        assertThat(isPhasedOut(efreet) != inGraveyard).isTrue();
    }

    private boolean isPhasedOut(Permanent permanent) {
        return gd.phasedOutPermanents.getOrDefault(player1.getId(), java.util.List.of()).contains(permanent);
    }

}
