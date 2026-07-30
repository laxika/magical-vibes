package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HuntedGhoulTest extends BaseCardTest {

    @Test
    @DisplayName("Hunted Ghoul can block a non-Human attacker")
    void canBlockNonHuman() {
        Permanent ghoul = new Permanent(new HuntedGhoul());
        ghoul.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ghoul);

        Permanent attacker = new Permanent(new GrizzlyBears()); // Bear
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(ghoul.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Hunted Ghoul cannot block a Human attacker")
    void cannotBlockHuman() {
        Permanent ghoul = new Permanent(new HuntedGhoul());
        ghoul.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(ghoul);

        Permanent attacker = new Permanent(new FugitiveWizard()); // Human Wizard
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures that aren't Humans");
    }
}
