package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OrcishCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Coin flip either pumps +2/+0 (win) or -0/-2 (loss) on the target Orc")
    void coinFlipAppliesBranch() {
        harness.addToBattlefield(player1, new OrcishCaptain());
        harness.addToBattlefield(player1, new OrcishArtillery());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Orcish Artillery");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();

        boolean won = target.getPowerModifier() == 2 && target.getToughnessModifier() == 0;
        boolean lost = target.getPowerModifier() == 0 && target.getToughnessModifier() == -2;
        assertThat(won != lost)
                .as("target must have exactly one of the +2/+0 (win) or -0/-2 (loss) branches")
                .isTrue();

        if (won) {
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("wins the coin flip"));
        } else {
            assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("loses the coin flip"));
        }
    }

    @Test
    @DisplayName("The pump wears off at end of turn")
    void pumpWearsOff() {
        harness.addToBattlefield(player1, new OrcishCaptain());
        harness.addToBattlefield(player1, new OrcishArtillery());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID targetId = harness.getPermanentId(player1, "Orcish Artillery");
        harness.activateAbility(player1, 0, null, targetId);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent target = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getId().equals(targetId))
                .findFirst().orElseThrow();
        assertThat(target.getPowerModifier()).isZero();
        assertThat(target.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("Cannot target a non-Orc creature")
    void cannotTargetNonOrc() {
        harness.addToBattlefield(player1, new OrcishCaptain());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        UUID bearId = harness.getPermanentId(player1, "Grizzly Bears");
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Orc creature");
    }
}
