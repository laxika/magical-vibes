package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JalumTome;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AetherjacketTest extends BaseCardTest {

    @Test
    @DisplayName("Pays the activation cost, sacrifices itself, and destroys another artifact")
    void sacrificesItselfAndDestroysAnotherArtifact() {
        addReadyAetherjacket(player1);
        harness.addToBattlefield(player2, new JalumTome());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, harness.getPermanentId(player2, "Jalum Tome"));
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Aetherjacket");
        harness.assertInGraveyard(player2, "Jalum Tome");
    }

    @Test
    @DisplayName("Cannot target itself or a non-artifact")
    void cannotTargetItselfOrNonArtifact() {
        Permanent source = addReadyAetherjacket(player1);
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player2, "Grizzly Bears")))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can activate only as a sorcery")
    void canActivateOnlyAsSorcery() {
        addReadyAetherjacket(player1);
        harness.addToBattlefield(player2, new JalumTome());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, 0, null, harness.getPermanentId(player2, "Jalum Tome")))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addReadyAetherjacket(Player player) {
        Permanent permanent = new Permanent(new Aetherjacket());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
