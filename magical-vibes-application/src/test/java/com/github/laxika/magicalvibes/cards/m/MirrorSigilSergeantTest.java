package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MirrorSigilSergeantTest extends BaseCardTest {

    private Permanent addReadySergeant(Player player) {
        Permanent perm = new Permanent(new MirrorSigilSergeant());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addBluePermanent(Player player) {
        Permanent perm = new Permanent(new FugitiveWizard());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
    }

    private void advanceToUpkeep(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
    }

    private long sergeantCount(Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Mirror-Sigil Sergeant"))
                .count();
    }

    @Test
    @DisplayName("Creates a token copy when controlling a blue permanent and accepting the may")
    void createsTokenCopyWhenAccepting() {
        addReadySergeant(player1);
        addBluePermanent(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability -> queues may prompt
        harness.handleMayAbilityChosen(player1, true);

        assertThat(sergeantCount(player1)).isEqualTo(2);
    }

    @Test
    @DisplayName("No token copy when declining the may")
    void noTokenWhenDeclining() {
        addReadySergeant(player1);
        addBluePermanent(player1);

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve triggered ability -> queues may prompt
        harness.handleMayAbilityChosen(player1, false);

        assertThat(sergeantCount(player1)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not trigger without a blue permanent (intervening if)")
    void doesNotTriggerWithoutBluePermanent() {
        addReadySergeant(player1);
        // No blue permanent controlled.

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(sergeantCount(player1)).isEqualTo(1);
    }
}
