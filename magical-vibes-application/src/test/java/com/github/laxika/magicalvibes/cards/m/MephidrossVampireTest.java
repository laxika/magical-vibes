package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.s.SkyhunterSkirmisher;
import com.github.laxika.magicalvibes.cards.v.VulshokSorcerer;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MephidrossVampire.class, SkyhunterSkirmisher.class, VulshokSorcerer.class})
class MephidrossVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Makes each creature you control a Vampire")
    void makesOwnCreaturesVampires() {
        addCreatureReady(player1, new MephidrossVampire());
        Permanent ownCreature = addCreatureReady(player1, new SkyhunterSkirmisher());
        Permanent opponentCreature = addCreatureReady(player2, new SkyhunterSkirmisher());

        assertThat(gqs.computeStaticBonus(gd, ownCreature).grantedSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(gqs.computeStaticBonus(gd, opponentCreature).grantedSubtypes())
                .doesNotContain(CardSubtype.VAMPIRE);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself after dealing combat damage to a creature")
    void putsCounterOnVampireAfterCombatDamage() {
        Permanent vampire = addCreatureReady(player1, new MephidrossVampire());
        addCreatureReady(player2, new SkyhunterSkirmisher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives another creature the noncombat-damage counter trigger")
    void putsCounterOnAnotherCreatureAfterNoncombatDamage() {
        addCreatureReady(player1, new MephidrossVampire());
        Permanent sorcerer = addCreatureReady(player1, new VulshokSorcerer());
        Permanent target = addCreatureReady(player2, new SkyhunterSkirmisher());

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(sorcerer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Does not put a counter on damage dealt to a player")
    void doesNotPutCounterOnDamageToPlayer() {
        addCreatureReady(player1, new MephidrossVampire());
        Permanent sorcerer = addCreatureReady(player1, new VulshokSorcerer());

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(sorcerer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Does not grant the counter trigger to an opponent's creature")
    void doesNotGrantCounterTriggerToOpponentsCreature() {
        Permanent vampire = addCreatureReady(player1, new MephidrossVampire());
        addCreatureReady(player2, new VulshokSorcerer());

        harness.activateAbility(player2, 0, null, vampire.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
