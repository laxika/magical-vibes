package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.j.JhovallRider;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CorruptOfficial.class, JhovallRider.class})
class CorruptOfficialTest extends BaseCardTest {

    @Test
    @DisplayName("Becoming blocked makes the defending player discard one card at random")
    void blockedMakesDefendingPlayerDiscard() {
        harness.setHand(player2, List.of(new JhovallRider()));
        addAttackingOfficial();
        addCreatureReady(player2, new JhovallRider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Jhovall Rider");
    }

    @Test
    @DisplayName("Becoming blocked by multiple creatures still triggers only once")
    void multipleBlockersTriggerOnce() {
        harness.setHand(player2, List.of(new JhovallRider(), new JhovallRider()));
        addAttackingOfficial();
        addCreatureReady(player2, new JhovallRider());
        addCreatureReady(player2, new JhovallRider());

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
        assertThat(official.isTapped()).isFalse();
    }

    @Test
    @DisplayName("The regeneration shield saves Corrupt Official from lethal combat damage")
    void regenerationShieldSavesFromLethalCombatDamage() {
        Permanent official = addAttackingOfficial();
        addCreatureReady(player2, new JhovallRider());
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        resolveCombat();

        harness.assertOnBattlefield(player1, "Corrupt Official");
        assertThat(official.isTapped()).isTrue();
        assertThat(official.getMarkedDamage()).isZero();
        assertThat(official.getRegenerationShield()).isZero();
    }

    @Test
    @DisplayName("An unblocked Corrupt Official causes no discard")
    void unblockedCausesNoDiscard() {
        JhovallRider rider = new JhovallRider();
        harness.setHand(player2, List.of(rider));
        addAttackingOfficial();

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).contains(rider);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private Permanent addAttackingOfficial() {
        Permanent permanent = addCreatureReady(player1, new CorruptOfficial());
        permanent.setAttacking(true);
        permanent.setAttackTarget(player2.getId());
        return permanent;
    }
}
