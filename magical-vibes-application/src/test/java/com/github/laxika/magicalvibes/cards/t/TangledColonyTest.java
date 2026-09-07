package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BatheInDragonfire;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TangledColony.class, BatheInDragonfire.class, GrizzlyBears.class})
class TangledColonyTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot block")
    void cannotBlock() {
        Permanent colony = addCreatureReady(player2, new TangledColony());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        assertThat(bls.canBlockAttacker(gd, colony, attacker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Creates one nonblocking Rat token per damage dealt to it this turn")
    void createsTokensEqualToDamageDealtThisTurn() {
        Permanent colony = harness.addToBattlefieldAndReturn(player2, new TangledColony());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new BatheInDragonfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, colony.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Tangled Colony");
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        List<Permanent> rats = findPermanents(player2, "Rat");
        assertThat(rats).hasSize(4);
        assertThat(bls.canBlockAttacker(gd, rats.getFirst(), attacker,
                gd.playerBattlefields.get(player1.getId()))).isFalse();
    }

    @Test
    @DisplayName("Triggers without creating tokens when no damage was dealt")
    void triggersWithNoDamage() {
        Permanent colony = harness.addToBattlefieldAndReturn(player1, new TangledColony());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, colony));
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Tangled Colony");
        assertThat(findPermanents(player1, "Rat")).isEmpty();
    }
}
