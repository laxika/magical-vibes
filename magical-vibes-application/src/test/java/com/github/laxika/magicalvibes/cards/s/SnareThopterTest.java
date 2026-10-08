package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HighspireArtisan;
import com.github.laxika.magicalvibes.cards.p.PrakhataPillarBug;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnareThopter.class, PrakhataPillarBug.class, HighspireArtisan.class})
class SnareThopterTest extends BaseCardTest {

    @Test
    @DisplayName("Haste allows Snare Thopter to attack immediately after entering")
    void hasteAllowsImmediateAttack() {
        harness.setHand(player1, List.of(new SnareThopter()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        declareAttackers(List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new SnareThopter());
        attacker.setAttacking(true);

        addCreatureReady(player2, new PrakhataPillarBug());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A flying creature can block Snare Thopter")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new SnareThopter());
        addCreatureReady(player2, new SnareThopter());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Snare Thopter");
        harness.assertInGraveyard(player2, "Snare Thopter");
    }

    @Test
    @DisplayName("A creature with reach can block Snare Thopter")
    void reachCreatureCanBlock() {
        addCreatureReady(player1, new SnareThopter());
        addCreatureReady(player2, new HighspireArtisan());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertLife(player2, 20);
        harness.assertOnBattlefield(player1, "Snare Thopter");
        harness.assertInGraveyard(player2, "Highspire Artisan");
    }
}
