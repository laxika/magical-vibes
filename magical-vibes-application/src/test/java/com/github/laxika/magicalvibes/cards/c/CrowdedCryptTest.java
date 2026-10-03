package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrowdedCrypt.class, GrizzlyBears.class, Shock.class})
class CrowdedCryptTest extends BaseCardTest {

    @Test
    @DisplayName("Adds black mana when tapped")
    void addsBlackMana() {
        Permanent crypt = addReadyCrypt();

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(crypt.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Gains a corpse counter whenever a creature you control dies")
    void gainsCorpseCounterWhenAllyCreatureDies() {
        Permanent crypt = addReadyCrypt();
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        destroyWithShock(bears.getId());
        harness.passBothPriorities();

        assertThat(crypt.getCounterCount(CounterType.CORPSE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates one decayed Zombie per corpse counter and sacrifices itself")
    void createsDecayedZombiesForCorpseCounters() {
        Permanent crypt = addReadyCrypt();
        crypt.setCounterCount(CounterType.CORPSE, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crypt);

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken())
                .allMatch(permanent -> permanent.getCard().getKeywords().contains(Keyword.DECAYED));
    }

    @Test
    @DisplayName("Opponent creature deaths do not add corpse counters")
    void ignoresOpponentCreatureDeaths() {
        Permanent crypt = addReadyCrypt();
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        destroyWithShock(bears.getId());
        resolveAllTriggers();

        assertThat(crypt.getCounterCount(CounterType.CORPSE)).isZero();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Zero corpse counters create no tokens but still require sacrificing the Crypt")
    void createsNoTokensWithZeroCounters() {
        Permanent crypt = addReadyCrypt();
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(crypt);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Zombie")).isZero();
        harness.assertInGraveyard(player1, "Crowded Crypt");
    }

    @Test
    @DisplayName("A decayed Zombie dying adds a corpse counter to another Crypt")
    void countsTokenCreatureDeaths() {
        createZombies(1);
        Permanent crypt = addReadyCrypt();
        Permanent zombie = findPermanent(player1, "Zombie");

        destroyWithShock(zombie.getId());
        resolveAllTriggers();

        assertThat(crypt.getCounterCount(CounterType.CORPSE)).isEqualTo(1);
        assertThat(countPermanents(player1, "Zombie")).isZero();
    }

    @Test
    @DisplayName("Created decayed Zombies cannot block")
    void decayedZombiesCannotBlock() {
        createZombies(1);
        Permanent attacker = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        attacker.setSummoningSick(false);
        declareAttackersAndPrepareBlockers(player2, List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player1,
                List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only attacking decayed Zombies are sacrificed at end of combat")
    void sacrificesOnlyAttackingZombiesAtEndOfCombat() {
        createZombies(2);
        List<Permanent> zombies = findPermanents(player1, "Zombie");
        zombies.forEach(zombie -> zombie.setSummoningSick(false));
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
            assertThat(gd.playerBattlefields.get(player1.getId())).containsAll(zombies);
        });
        harness.passUntil(player1, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .doesNotContain(zombies.get(0)).contains(zombies.get(1));
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("Decayed sacrifice waits for its delayed trigger to resolve")
    void decayedSacrificeUsesTheStackAtEndOfCombat() {
        createZombies(1);
        Permanent zombie = findPermanent(player1, "Zombie");
        zombie.setSummoningSick(false);
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0));
            resolveAllTriggers();
        });

        harness.passUntil(player1, TurnStep.END_OF_COMBAT);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(zombie);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(zombie);
    }

    private void createZombies(int count) {
        Permanent crypt = addReadyCrypt();
        crypt.setCounterCount(CounterType.CORPSE, count);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();
    }

    private Permanent addReadyCrypt() {
        Permanent crypt = harness.addToBattlefieldAndReturn(player1, new CrowdedCrypt());
        crypt.setSummoningSick(false);
        return crypt;
    }

    private void destroyWithShock(UUID targetId) {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castAndResolveInstant(player2, 0, targetId);
    }
}
