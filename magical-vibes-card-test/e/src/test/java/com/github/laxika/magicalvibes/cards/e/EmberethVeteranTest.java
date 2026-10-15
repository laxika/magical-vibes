package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.m.MonstrousRage;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({EmberethVeteran.class, MonstrousRage.class})
class EmberethVeteranTest extends BaseCardTest {

    @Test
    void sacrificesAndCreatesYoungHeroRoleAttachedToAnotherCreature() {
        addCreatureReady(player1, new EmberethVeteran());
        Permanent target = addCreatureReady(player1, new EmberethVeteran());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.assertInGraveyard(player1, "Embereth Veteran");
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Young Hero");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void canAttachYoungHeroRoleToAnOpponentsCreature() {
        addCreatureReady(player1, new EmberethVeteran());
        Permanent target = addCreatureReady(player2, new EmberethVeteran());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        Permanent role = findPermanent(player1, "Young Hero");
        assertThat(role.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void cannotTargetTheVeteranItself() {
        Permanent veteran = addCreatureReady(player1, new EmberethVeteran());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, veteran.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be another creature");
        harness.assertOnBattlefield(player1, "Embereth Veteran");
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 2, 3})
    void youngHeroAttackChecksToughnessThreshold(int startingCounters) {
        Permanent target = createEnchantedVeteran();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, startingCounters);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                .isEqualTo(startingCounters + (startingCounters <= 2 ? 1 : 0));
    }

    @Test
    void youngHeroRechecksToughnessWhenAttackTriggerResolves() {
        Permanent target = createEnchantedVeteran();
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new MonstrousRage()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));
        assertThat(gd.stack).hasSize(1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(4);
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void newerRoleReplacesOlderRoleControlledBySamePlayer() {
        Permanent target = createEnchantedVeteran();
        Permanent oldRole = findPermanent(player1, "Young Hero");
        addCreatureReady(player1, new EmberethVeteran());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 2, null, target.getId());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Young Hero")).hasSize(1);
        Permanent newRole = findPermanent(player1, "Young Hero");
        assertThat(newRole.getId()).isNotEqualTo(oldRole.getId());
        assertThat(newRole.getAttachedTo()).isEqualTo(target.getId());
    }

    @Test
    void opponentsEnchantedCreatureGetsCounterWhenItAttacks() {
        addCreatureReady(player1, new EmberethVeteran());
        Permanent target = addCreatureReady(player2, new EmberethVeteran());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(player2, List.of(0)));
        resolveAllTriggers();

        assertThat(target.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void createsNoRoleIfTargetLeavesBeforeResolution() {
        addCreatureReady(player1, new EmberethVeteran());
        Permanent target = addCreatureReady(player2, new EmberethVeteran());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Young Hero")).isEmpty();
        harness.assertInGraveyard(player1, "Embereth Veteran");
    }

    private Permanent createEnchantedVeteran() {
        addCreatureReady(player1, new EmberethVeteran());
        Permanent target = addCreatureReady(player1, new EmberethVeteran());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        return target;
    }
}
