package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.j.Juggernaut;
import com.github.laxika.magicalvibes.cards.r.RodOfRuin;
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

class SphereOfPurityTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 damage from an artifact source")
    void preventsDamageFromArtifactSource() {
        harness.addToBattlefield(player1, new SphereOfPurity());
        addReadyRod(player2);
        harness.setLife(player1, 20);
        harness.addMana(player2, ManaColor.WHITE, 3);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not prevent damage from a non-artifact source")
    void doesNotPreventNonArtifactDamage() {
        harness.addToBattlefield(player1, new SphereOfPurity());
        harness.setLife(player1, 20);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents 1 of combat damage from an artifact creature")
    void preventsArtifactCombatDamage() {
        harness.addToBattlefield(player1, new SphereOfPurity());
        harness.setLife(player1, 20);

        Permanent juggernaut = new Permanent(new Juggernaut());
        juggernaut.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(juggernaut);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        gs.declareAttackers(gd, player2, List.of(0));

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player1, List.<BlockerAssignment>of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    private Permanent addReadyRod(Player player) {
        Permanent rod = new Permanent(new RodOfRuin());
        rod.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(rod);
        return rod;
    }
}
