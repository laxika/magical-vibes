package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MammothHarnessTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature loses flying")
    void enchantedCreatureLosesFlying() {
        Permanent flyer = addCreatureReady(player1, new AirElemental());
        assertThat(gqs.hasKeyword(gd, flyer, Keyword.FLYING)).isTrue();

        addHarnessAttachedTo(player1, flyer);

        assertThat(gqs.hasKeyword(gd, flyer, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Flying returns once the Harness leaves the enchanted creature")
    void flyingReturnsWhenHarnessRemoved() {
        Permanent flyer = addCreatureReady(player1, new AirElemental());
        Permanent harnessPerm = addHarnessAttachedTo(player1, flyer);

        gd.playerBattlefields.get(player1.getId()).remove(harnessPerm);

        assertThat(gqs.hasKeyword(gd, flyer, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("When the enchanted creature blocks, the blocked attacker gains first strike")
    void enchantedCreatureBlocks_attackerGainsFirstStrike() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent harnessPerm = addHarnessAttachedTo(player2, blocker);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).anyMatch(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Mammoth Harness")
                        && attacker.getId().equals(se.getTargetId())
                        && harnessPerm.getId().equals(se.getSourcePermanentId()));

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("When the enchanted creature becomes blocked, the blocker gains first strike")
    void enchantedCreatureBecomesBlocked_blockerGainsFirstStrike() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addHarnessAttachedTo(player1, attacker);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, blocker, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, attacker, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("No trigger when the Harness is on the battlefield but not attached")
    void noTriggerWhenNotAttached() {
        addCreatureReady(player2, new GrizzlyBears());
        addHarness(player2);

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Mammoth Harness"));
    }

    private Permanent addHarness(Player player) {
        Permanent perm = new Permanent(new MammothHarness());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addHarnessAttachedTo(Player player, Permanent creature) {
        Permanent perm = addHarness(player);
        perm.setAttachedTo(creature.getId());
        return perm;
    }
}
