package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.b.BakersbaneDuo;
import com.github.laxika.magicalvibes.cards.l.LullmagesFamiliar;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({RustShieldRampager.class, BakersbaneDuo.class, LullmagesFamiliar.class})
class RustShieldRampagerTest extends BaseCardTest {

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new RustShieldRampager()));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void offspringDoesNotCreateTokenWhenNotPaid() {
        harness.setHand(player1, List.of(new RustShieldRampager()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
    }

    @Test
    void cannotBeBlockedByCreatureWithPowerTwoOrLess() {
        Permanent blocker = addCreatureReady(player2, new BakersbaneDuo());
        Permanent rampager = addCreatureReady(player1, new RustShieldRampager());
        rampager.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(rampager);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void canBeBlockedByCreatureWithPowerThreeOrGreater() {
        Permanent blocker = addCreatureReady(player2, new RustShieldRampager());
        Permanent rampager = addCreatureReady(player1, new RustShieldRampager());
        rampager.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(rampager);

        gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void creatureWithPowerTwoCanBlockAfterReceivingACounter() {
        Permanent blocker = addCreatureReady(player2, new BakersbaneDuo());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent rampager = addCreatureReady(player1, new RustShieldRampager());
        rampager.setAttacking(true);
        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void offspringTokenRetainsBlockingRestrictionWithoutCreatingMoreOffspring() {
        harness.setHand(player1, List.of(new RustShieldRampager()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        Permanent token = tokens.getFirst();
        token.setSummoningSick(false);
        token.setAttacking(true);
        addCreatureReady(player2, new BakersbaneDuo());
        prepareDeclareBlockers();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(token);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void payingOffspringDoesNotTriggerKickedSpellAbilities() {
        addCreatureReady(player1, new LullmagesFamiliar());
        harness.setHand(player1, List.of(new RustShieldRampager()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().isToken()).hasSize(1);
    }
}
