package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BlindPhantasm;
import com.github.laxika.magicalvibes.cards.h.HomingSliver;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VirulentSliver.class, HomingSliver.class, BlindPhantasm.class})
class VirulentSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Virulent Sliver gives poisonous 1 to itself and another Sliver")
    void sliversGivePoisonCounters() {
        Permanent virulentSliver = addCreatureReady(player1, new VirulentSliver());
        Permanent otherSliver = addCreatureReady(player1, new HomingSliver());
        virulentSliver.setAttacking(true);
        otherSliver.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Virulent Sliver gives poisonous 1 to opposing Slivers")
    void opposingSliversGetPoisonous() {
        addCreatureReady(player1, new VirulentSliver());
        Permanent opposingSliver = addCreatureReady(player2, new HomingSliver());
        opposingSliver.setAttacking(true);

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Virulent Sliver does not give poisonous 1 to non-Slivers")
    void nonSliversDoNotGetPoisonous() {
        addCreatureReady(player1, new VirulentSliver());
        Permanent nonSliver = addCreatureReady(player1, new BlindPhantasm());
        nonSliver.setAttacking(true);

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("A blocked Sliver deals no poisonous damage to the defending player")
    void blockedSliversDoNotGivePoisonCounters() {
        addCreatureReady(player1, new VirulentSliver());
        Permanent attackingSliver = addCreatureReady(player1, new HomingSliver());
        attackingSliver.setAttacking(true);
        addCreatureReady(player2, new BlindPhantasm());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Multiple Virulent Slivers grant independently triggering instances of poisonous")
    void multipleInstancesOfPoisonousTriggerSeparately() {
        addCreatureReady(player1, new VirulentSliver());
        addCreatureReady(player2, new VirulentSliver());
        Permanent attacker = addCreatureReady(player1, new HomingSliver());
        attacker.setAttacking(true);

        resolveCombat(player1);

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);

        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(2);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("Poisonous triggers still resolve after the granting and attacking Slivers leave")
    void poisonousTriggersSurviveSliversLeavingBattlefield() {
        Permanent grantor = addCreatureReady(player1, new VirulentSliver());
        Permanent attacker = addCreatureReady(player1, new HomingSliver());
        attacker.setAttacking(true);

        resolveCombat(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        gd.playerBattlefields.get(player1.getId()).remove(grantor);
        gd.playerBattlefields.get(player1.getId()).remove(attacker);
        gd.playerGraveyards.get(player1.getId()).add(grantor.getCard());
        gd.playerGraveyards.get(player1.getId()).add(attacker.getCard());

        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Slivers lose the poisonous grant when Virulent Sliver leaves before combat damage")
    void grantEndsWhenVirulentSliverLeaves() {
        Permanent grantor = addCreatureReady(player1, new VirulentSliver());
        Permanent attacker = addCreatureReady(player1, new HomingSliver());
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).remove(grantor);
        gd.playerGraveyards.get(player1.getId()).add(grantor.getCard());

        resolveCombat(player1);
        resolveAllTriggers();

        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }
}
