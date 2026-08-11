package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class LoafingGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking mills a card and prevents the Giant's combat damage when it is a land")
    void attackingWithLandMilledPreventsDamage() {
        Permanent giant = addCreatureReady(player1, new LoafingGiant());
        harness.setLibrary(player1, List.of(new Forest()));
        int startingLife = gd.getLife(player2.getId());

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(startingLife);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(giant.getId());
    }

    @Test
    @DisplayName("Attacking does not prevent combat damage when the milled card is not a land")
    void attackingWithNonlandMilledDealsDamage() {
        addCreatureReady(player1, new LoafingGiant());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Blocking mills a card and prevents the Giant's combat damage when it is a land")
    void blockingWithLandMilledPreventsGiantDamage() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player2, new LoafingGiant());
        harness.setLibrary(player2, List.of(new Forest()));

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(giant.getId());
    }
}
