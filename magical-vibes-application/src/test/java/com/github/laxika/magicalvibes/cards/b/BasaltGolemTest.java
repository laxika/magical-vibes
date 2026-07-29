package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.action.SacrificeAtEndOfCombat;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BasaltGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Basalt Golem can't be blocked by an artifact creature")
    void cannotBeBlockedByArtifactCreature() {
        Permanent golem = addReady(player1, new BasaltGolem());
        golem.setAttacking(true);
        Permanent thopter = addReady(player2, new Ornithopter());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(thopter);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(golem);
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Basalt Golem can be blocked by a nonartifact creature")
    void canBeBlockedByNonartifactCreature() {
        Permanent golem = addReady(player1, new BasaltGolem());
        golem.setAttacking(true);
        Permanent spider = addReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(spider.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Becoming blocked schedules the blocker for sacrifice at end of combat")
    void becomesBlockedSchedulesSacrifice() {
        Permanent golem = addReady(player1, new BasaltGolem());
        golem.setAttacking(true);
        Permanent spider = addReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Basalt Golem")
                        && spider.getId().equals(se.getTargetId()));

        harness.passBothPriorities();
        assertThat(gd.getDelayedActions(SacrificeAtEndOfCombat.class))
                .anyMatch(a -> a.permanentId().equals(spider.getId()));
    }

    @Test
    @DisplayName("The blocker survives combat damage, is sacrificed at end of combat, and its controller gets a Wall token")
    void blockerSacrificedAtEndOfCombatAndControllerGetsWall() {
        Permanent golem = addReady(player1, new BasaltGolem());
        golem.setAttacking(true);
        addReady(player2, new GiantSpider()); // 2/4 survives the Golem's 2 damage

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");

        List<Permanent> defenderBattlefield = gd.playerBattlefields.get(player2.getId());
        assertThat(defenderBattlefield).anyMatch(p -> p.getCard().getName().equals("Wall"));
        Permanent wall = defenderBattlefield.stream()
                .filter(p -> p.getCard().getName().equals("Wall")).findFirst().orElseThrow();
        assertThat(wall.getCard().getPower()).isZero();
        assertThat(wall.getCard().getToughness()).isEqualTo(2);
    }

    @Test
    @DisplayName("No Wall token is created when the blocker died to combat damage before end of combat")
    void noWallTokenWhenBlockerAlreadyDied() {
        Permanent golem = addReady(player1, new BasaltGolem());
        golem.setAttacking(true);
        addReady(player2, new GrizzlyBears()); // 2/2, dies to the Golem's 2 damage

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Wall"));
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
