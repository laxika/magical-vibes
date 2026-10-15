package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.b.BarbaryApes;
import com.github.laxika.magicalvibes.cards.k.KeepersOfTheFaith;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.MountainYeti;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Crevasse.class, MountainYeti.class, CatWarriors.class, Mountain.class, Forest.class,
        BarbaryApes.class, KeepersOfTheFaith.class})
class CrevasseTest extends BaseCardTest {

    @Test
    @DisplayName("Mountainwalk can be blocked while Crevasse is on the battlefield")
    void mountainwalkCanBeBlocked() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Crevasse());
        Permanent attacker = addCreatureReady(player1, new MountainYeti());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Crevasse does not affect other landwalk abilities")
    void otherLandwalkRemainsUnblockable() {
        harness.addToBattlefield(player2, new Forest());
        harness.addToBattlefield(player2, new Crevasse());
        Permanent attacker = addCreatureReady(player1, new CatWarriors());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mountainwalk remains unblockable without Crevasse")
    void mountainwalkRemainsUnblockableWithoutCrevasse() {
        harness.addToBattlefield(player2, new Mountain());
        Permanent attacker = addCreatureReady(player1, new MountainYeti());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Crevasse works when controlled by the attacking player")
    void mountainwalkCanBeBlockedWhenAttackerControlsCrevasse() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player1, new Crevasse());
        Permanent attacker = addCreatureReady(player1, new MountainYeti());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Crevasse does not let white creatures block a creature with protection from white")
    void protectionStillPreventsBlocking() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Crevasse());
        Permanent attacker = addCreatureReady(player1, new MountainYeti());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new KeepersOfTheFaith());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Mountainwalk prevents blocking again after Crevasse leaves the battlefield")
    void mountainwalkReturnsWhenCrevasseLeaves() {
        harness.addToBattlefield(player2, new Mountain());
        harness.addToBattlefield(player2, new Crevasse());
        Permanent crevasse = findPermanent(player2, "Crevasse");
        gd.playerBattlefields.get(player2.getId()).remove(crevasse);
        gd.playerGraveyards.get(player2.getId()).add(crevasse.getCard());
        Permanent attacker = addCreatureReady(player1, new MountainYeti());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Crevasse in hand does not suppress mountainwalk")
    void crevasseInHandDoesNotAffectBlocking() {
        harness.addToBattlefield(player2, new Mountain());
        harness.setHand(player2, List.of(new Crevasse()));
        Permanent attacker = addCreatureReady(player1, new MountainYeti());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new BarbaryApes());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class);
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
