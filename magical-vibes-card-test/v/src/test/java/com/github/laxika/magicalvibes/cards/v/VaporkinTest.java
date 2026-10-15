package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.FleetfeatherSandals;
import com.github.laxika.magicalvibes.cards.p.PheresBandCentaurs;
import com.github.laxika.magicalvibes.cards.p.PrescientChimera;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Vaporkin.class, PrescientChimera.class, PheresBandCentaurs.class, FleetfeatherSandals.class})
class VaporkinTest extends BaseCardTest {

    @Test
    @DisplayName("Vaporkin can block a creature with flying")
    void canBlockFlyingCreature() {
        Permanent vaporkin = addCreatureReady(player2, new Vaporkin());

        Permanent attacker = addCreatureReady(player1, new PrescientChimera());
        attacker.setAttacking(true);

        declareBlocker();

        assertThat(vaporkin.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vaporkin cannot block a creature without flying")
    void cannotBlockNonFlyingCreature() {
        addCreatureReady(player2, new Vaporkin());

        Permanent attacker = addCreatureReady(player1, new PheresBandCentaurs());
        attacker.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only block creatures with flying");
    }

    @Test
    @DisplayName("Vaporkin can block a creature granted flying by equipment")
    void canBlockCreatureWithGrantedFlying() {
        Permanent vaporkin = addCreatureReady(player2, new Vaporkin());
        Permanent attacker = addCreatureReady(player1, new PheresBandCentaurs());
        attacker.setAttacking(true);
        Permanent sandals = harness.addToBattlefieldAndReturn(player1, new FleetfeatherSandals());
        sandals.setAttachedTo(attacker.getId());

        declareBlocker();

        assertThat(vaporkin.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness does not prevent Vaporkin from blocking a flyer")
    void canBlockWhileSummoningSick() {
        Permanent vaporkin = harness.addToBattlefieldAndReturn(player2, new Vaporkin());
        vaporkin.setSummoningSick(true);
        Permanent attacker = addCreatureReady(player1, new PrescientChimera());
        attacker.setAttacking(true);

        declareBlocker();

        assertThat(vaporkin.isBlocking()).isTrue();
    }

    private void declareBlocker() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
    }
}
