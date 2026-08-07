package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class EyeblightMassacreTest extends BaseCardTest {

    @Test
    @DisplayName("Gives non-Elf creatures on both sides -2/-2 and leaves Elves untouched")
    void weakensOnlyNonElves() {
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        Permanent enemyElf = addCreatureReady(player2, new LlanowarElves());
        harness.setHand(player1, List.of(new EyeblightMassacre()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, enemyElf)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, enemyElf)).isEqualTo(1);
        // The 2/2 Bears drops to 0/0 and is put into the graveyard by state-based actions.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(ownBear.getId()));
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The -2/-2 wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent giant = addCreatureReady(player2, new HillGiant());
        harness.setHand(player1, List.of(new EyeblightMassacre()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castSorcery(player1, 0, (UUID) null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, giant)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, giant)).isEqualTo(3);
    }
}
