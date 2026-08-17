package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CrookedScalesTest extends BaseCardTest {

    @Test
    @DisplayName("Ability requires one creature controlled by each side")
    void requiresOneCreatureControlledByEachSide() {
        Permanent scales = addCreatureReady(player1, new CrookedScales());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scales),
                0,
                List.of(ownCreature.getId(), ownCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability destroys one of the chosen creatures after its flip process")
    void destroysChosenCreatureAfterFlipProcess() {
        Permanent scales = addCreatureReady(player1, new CrookedScales());
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.activateAbilityWithMultiTargets(
                player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(scales),
                0,
                List.of(ownCreature.getId(), opposingCreature.getId()));
        harness.passBothPriorities();

        if (!gd.pendingMayAbilities.isEmpty()) {
            harness.handleMayAbilityChosen(player1, true);
        }
        if (!gd.pendingMayAbilities.isEmpty()) {
            harness.handleMayAbilityChosen(player1, false);
        }

        List<String> flipLogs = gd.gameLog.stream()
                .map(GameLogEntry::plainText)
                .filter(log -> log.contains("coin flip for Crooked Scales"))
                .toList();
        assertThat(flipLogs).isNotEmpty();

        boolean lastFlipWon = flipLogs.getLast().contains("wins the coin flip");
        assertThat(gd.playerBattlefields.get(player1.getId()).contains(ownCreature))
                .isEqualTo(lastFlipWon);
        assertThat(gd.playerBattlefields.get(player2.getId()).contains(opposingCreature))
                .isEqualTo(!lastFlipWon);
    }
}
