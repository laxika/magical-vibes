package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PretendersClaimTest extends BaseCardTest {

    @Test
    @DisplayName("When enchanted creature becomes blocked, all defending lands are tapped")
    void becomesBlockedTapsDefendingLands() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        addClaimAttachedTo(player1, attacker);
        Permanent ownLand = addLand(player1);
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent defendingLand = addLand(player2);

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(defendingLand.isTapped()).isTrue();
        assertThat(blocker.isTapped()).isFalse();
        assertThat(ownLand.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Becomes-blocked trigger fires once for multiple blockers")
    void becomesBlockedTriggersOnlyOnce() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent claim = addClaimAttachedTo(player1, attacker);
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());
        Permanent defendingLand = addLand(player2);

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        assertThat(gd.stack).filteredOn(se ->
                se.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                        && se.getCard().getName().equals("Pretender's Claim")
                        && se.getSourcePermanentId().equals(claim.getId()))
                .hasSize(1);

        harness.passBothPriorities();
        assertThat(defendingLand.isTapped()).isTrue();
    }

    @Test
    @DisplayName("An unattached Pretender's Claim does not trigger")
    void unattachedClaimDoesNotTrigger() {
        addCreatureReady(player1, new GrizzlyBears());
        addClaim(player1);
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player1, List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).noneMatch(se -> se.getCard().getName().equals("Pretender's Claim"));
    }

    private Permanent addClaim(Player player) {
        Permanent claim = new Permanent(new PretendersClaim());
        claim.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(claim);
        return claim;
    }

    private Permanent addClaimAttachedTo(Player player, Permanent creature) {
        Permanent claim = addClaim(player);
        claim.setAttachedTo(creature.getId());
        return claim;
    }

    private Permanent addLand(Player player) {
        Permanent land = new Permanent(new Forest());
        land.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(land);
        return land;
    }
}
