package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class OgreLeadfootTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked by an artifact creature destroys that creature")
    void becomesBlockedByArtifactCreatureDestroysIt() {
        Permanent ogre = addReadyOgre(player1);
        ogre.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new Ornithopter());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(blocker.getId());
        assertThat(entry.isNonTargeting()).isTrue();

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        harness.assertInGraveyard(player2, "Ornithopter");
    }

    @Test
    @DisplayName("Becoming blocked by a nonartifact creature does not trigger")
    void becomesBlockedByNonartifactCreatureDoesNotTrigger() {
        Permanent ogre = addReadyOgre(player1);
        ogre.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(entry ->
                entry.getCard().getName().equals("Ogre Leadfoot"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(blocker.getId()));
    }

    @Test
    @DisplayName("With mixed blockers, only the artifact creature is destroyed")
    void mixedBlockersOnlyArtifactCreatureIsDestroyed() {
        Permanent ogre = addReadyOgre(player1);
        ogre.setAttacking(true);
        Permanent artifactBlocker = addCreatureReady(player2, new Ornithopter());
        Permanent nonartifactBlocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.stack).filteredOn(entry ->
                entry.getCard().getName().equals("Ogre Leadfoot"))
                .hasSize(1);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Ornithopter");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(nonartifactBlocker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Ornithopter"))
                .hasSize(1);
    }

    private Permanent addReadyOgre(Player player) {
        Permanent perm = new Permanent(new OgreLeadfoot());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
