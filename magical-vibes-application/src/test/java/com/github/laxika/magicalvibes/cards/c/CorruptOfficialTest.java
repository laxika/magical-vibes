package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CorruptOfficialTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked makes the defending player discard one card at random")
    void blockedMakesDefendingPlayerDiscard() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest())));
        addAttackingOfficial();
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures still triggers only once")
    void multipleBlockersTriggerOnce() {
        harness.setHand(player2, new ArrayList<>(List.of(new Forest(), new GrizzlyBears())));
        addAttackingOfficial();
        addCreatureReady(player2, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The regeneration ability creates a regeneration shield")
    void regeneratesItself() {
        Permanent official = addCreatureReady(player1, new CorruptOfficial());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(official.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("An unblocked Corrupt Official causes no discard")
    void unblockedCausesNoDiscard() {
        Forest forest = new Forest();
        harness.setHand(player2, new ArrayList<>(List.of(forest)));
        addAttackingOfficial();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(forest);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private Permanent addAttackingOfficial() {
        Permanent permanent = addCreatureReady(player1, new CorruptOfficial());
        permanent.setAttacking(true);
        permanent.setAttackTarget(player2.getId());
        return permanent;
    }
}
