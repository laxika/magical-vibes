package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.IxidorRealitySculptor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StormcragElemental.class, IxidorRealitySculptor.class})
class StormcragElementalTest extends BaseCardTest {

    @Test
    void castingFaceUpDoesNotGiveAMegamorphCounter() {
        harness.setHand(player1, List.of(new StormcragElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Stormcrag Elemental");
        assertThat(elemental.isFaceDown()).isFalse();
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void castingFaceDownMakesATwoTwoWithoutTrample() {
        harness.setHand(player1, List.of(new StormcragElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Stormcrag Elemental");
        assertThat(elemental.isFaceDown()).isTrue();
        assertThat(gqs.getEffectivePower(gd, elemental)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elemental)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, elemental, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void turningFaceUpWithIxidorDoesNotGiveAMegamorphCounter() {
        addCreatureReady(player1, new IxidorRealitySculptor());
        Permanent elemental = addCreatureReady(player1, new StormcragElemental());
        elemental.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, elemental.getId());
        harness.passBothPriorities();

        assertThat(elemental.isFaceDown()).isFalse();
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void faceUpElementalTramplesOverAFaceDownBlocker() {
        Permanent attacker = addCreatureReady(player1, new StormcragElemental());
        Permanent blocker = addCreatureReady(player2, new StormcragElemental());
        blocker.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setLife(player2, 20);
        attacker.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 3));

        harness.assertLife(player2, 17);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
    }

    @Test
    void megamorphPutsPlusOneCounterOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new StormcragElemental()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent elemental = findPermanent(player1, "Stormcrag Elemental");
        assertThat(elemental.isFaceDown()).isTrue();
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(elemental));

        assertThat(elemental.isFaceDown()).isFalse();
        assertThat(elemental.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
