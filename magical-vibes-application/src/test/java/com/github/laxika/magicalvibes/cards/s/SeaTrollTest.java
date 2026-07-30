package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SeaTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Regenerates after being blocked by a blue creature")
    void regeneratesAfterBeingBlockedByBlueCreature() {
        Permanent troll = addSeaTroll(player1);
        troll.setAttacking(true);
        addCreatureReady(player2, new AirElemental());

        declareBlock();

        activateRegeneration(player1);

        assertThat(findPermanent(player1, "Sea Troll").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regenerates after blocking a blue creature")
    void regeneratesAfterBlockingBlueCreature() {
        Permanent attacker = addCreatureReady(player1, new FugitiveWizard());
        attacker.setAttacking(true);
        addSeaTroll(player2);

        declareBlock();

        harness.forceActivePlayer(player2);
        activateRegeneration(player2);

        assertThat(findPermanent(player2, "Sea Troll").getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot regenerate when the blocking creature was not blue")
    void cannotRegenerateAgainstNonBlueBlocker() {
        Permanent troll = addSeaTroll(player1);
        troll.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        declareBlock();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot regenerate when it was not in combat with a creature this turn")
    void cannotRegenerateWithoutCombat() {
        addSeaTroll(player1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void declareBlock() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }

    private void activateRegeneration(Player player) {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.activateAbility(player, 0, null, null);
        harness.passBothPriorities();
    }

    private Permanent addSeaTroll(Player player) {
        Permanent perm = new Permanent(new SeaTroll());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
