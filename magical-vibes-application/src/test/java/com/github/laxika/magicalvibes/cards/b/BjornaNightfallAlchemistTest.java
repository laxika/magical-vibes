package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BjornaNightfallAlchemist.class, GrizzlyBears.class, LeoninScimitar.class})
class BjornaNightfallAlchemistTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices an artifact, deals 1 damage, and goads the target creature")
    void sacrificesArtifactDamagesAndGoadsTarget() {
        addReadyBjorna(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LeoninScimitar());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Leonin Scimitar");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must attack");

        int targetIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);
        gs.declareAttackers(gd, player1, List.of(targetIndex));
    }

    @Test
    @DisplayName("A creature that is not the target is not goaded")
    void onlyTargetCreatureIsGoaded() {
        addReadyBjorna(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new LeoninScimitar());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        int targetIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);
        gs.declareAttackers(gd, player1, List.of(targetIndex));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without an artifact to sacrifice")
    void cannotActivateWithoutArtifact() {
        addReadyBjorna(player1);
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBjorna(Player player) {
        return addCreatureReady(player, new BjornaNightfallAlchemist());
    }
}
