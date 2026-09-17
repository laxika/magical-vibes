package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.Lunge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DesertNomads.class, Desert.class, GrizzlyBears.class, Lunge.class})
class DesertNomadsTest extends BaseCardTest {

    @Test
    @DisplayName("Desertwalk prevents blocking while the defender controls a Desert")
    void desertwalkPreventsBlocking() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new Desert());
        Permanent attacker = addCreatureReady(player1, new DesertNomads());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Desertwalk allows blocking when the defender controls no Desert")
    void desertwalkAllowsBlockingWithoutDesert() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new DesertNomads());
        attacker.setAttacking(true);

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Prevents damage dealt by a Desert")
    void preventsDamageDealtByDesert() {
        Permanent desert = harness.addToBattlefieldAndReturn(player1, new Desert());
        Permanent nomads = addCreatureReady(player2, new DesertNomads());
        nomads.setAttacking(true);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_OF_COMBAT);
        harness.activateAbility(player1, indexOf(player1, desert), 1, null, nomads.getId());
        harness.passBothPriorities();

        assertThat(nomads.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Desert Nomads");
    }

    @Test
    @DisplayName("Does not prevent damage from a non-Desert source")
    void doesNotPreventDamageFromNonDesertSource() {
        Permanent nomads = addCreatureReady(player2, new DesertNomads());
        harness.setHand(player1, List.of(new Lunge()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, List.of(nomads.getId(), player2.getId()));
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Desert Nomads");
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
