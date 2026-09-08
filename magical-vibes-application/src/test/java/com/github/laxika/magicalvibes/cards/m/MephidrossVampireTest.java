package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.c.CloudSprite;
import com.github.laxika.magicalvibes.cards.f.FemerefArchers;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.s.SkyhunterSkirmisher;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class MephidrossVampireTest extends BaseCardTest {

    @Test
    @DisplayName("Makes each creature you control a Vampire")
    void makesOwnCreaturesVampires() {
        addCreatureReady(player1, new MephidrossVampire());
        Permanent ownCreature = addCreatureReady(player1, new SerraAngel());
        Permanent opponentCreature = addCreatureReady(player2, new SerraAngel());

        assertThat(gqs.computeStaticBonus(gd, ownCreature).grantedSubtypes()).contains(CardSubtype.VAMPIRE);
        assertThat(gqs.computeStaticBonus(gd, opponentCreature).grantedSubtypes())
                .doesNotContain(CardSubtype.VAMPIRE);
    }

    @Test
    @DisplayName("Puts a +1/+1 counter on itself after dealing combat damage to a creature")
    void putsCounterOnVampireAfterCombatDamage() {
        Permanent vampire = addCreatureReady(player1, new MephidrossVampire());
        vampire.setAttacking(true);
        addCreatureReady(player2, new CloudSprite());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(vampire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gives another creature the noncombat-damage counter trigger")
    void putsCounterOnAnotherCreatureAfterNoncombatDamage() {
        addCreatureReady(player1, new MephidrossVampire());
        Permanent archers = addCreatureReady(player1, new FemerefArchers());
        Permanent target = addCreatureReady(player2, new SkyhunterSkirmisher());
        target.setAttacking(true);

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(archers.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
