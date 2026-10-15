package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.c.ChandraNalaar;
import com.github.laxika.magicalvibes.cards.b.BurstLightning;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.p.PillarfieldOx;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({NobleVestige.class, ChandraNalaar.class, Forest.class, GrizzlyBears.class,
        Shock.class, BurstLightning.class, PillarfieldOx.class})
class NobleVestigeTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents the next 1 damage dealt to a target player")
    void preventsNextDamageToPlayer() {
        Permanent nobleVestige = addCreatureReady(player1, new NobleVestige());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(nobleVestige.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Prevents the next 1 damage dealt to a target planeswalker")
    void preventsNextDamageToPlaneswalker() {
        addCreatureReady(player1, new NobleVestige());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraNalaar());
        planeswalker.setCounterCount(CounterType.LOYALTY, 5);

        harness.activateAbility(player1, 0, null, planeswalker.getId());
        harness.passBothPriorities();

        harness.setHand(player2, java.util.List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(4);
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        addCreatureReady(player1, new NobleVestige());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void shieldIsConsumedByTheFirstDamageEvent() {
        addCreatureReady(player1, new NobleVestige());
        harness.setLife(player2, 20);
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.setHand(player1, java.util.List.of(new BurstLightning(), new BurstLightning()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
    }

    @Test
    void multipleActivationsPreventOneDamageEach() {
        addCreatureReady(player1, new NobleVestige());
        addCreatureReady(player1, new NobleVestige());
        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.setHand(player2, java.util.List.of(new BurstLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void cannotTargetCreature() {
        addCreatureReady(player1, new NobleVestige());
        Permanent creature = addCreatureReady(player2, new PillarfieldOx());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void unusedShieldExpiresAtEndOfTurn() {
        addCreatureReady(player1, new NobleVestige());
        harness.setLife(player1, 20);
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, java.util.List.of(new BurstLightning()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    void cannotActivateWhileSummoningSick() {
        harness.addToBattlefield(player1, new NobleVestige());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

}
