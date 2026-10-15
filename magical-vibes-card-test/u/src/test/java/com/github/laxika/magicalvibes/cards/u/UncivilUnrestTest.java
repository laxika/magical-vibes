package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GrimHaruspex;
import com.github.laxika.magicalvibes.cards.k.KrenkoMobBoss;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UncivilUnrest.class, GrizzlyBears.class, ProdigalSorcerer.class, GrimHaruspex.class,
        KrenkoMobBoss.class})
class UncivilUnrestTest extends BaseCardTest {

    @Test
    @DisplayName("Nontoken creatures you control get riot")
    void grantsRiotToNontokenCreatures() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Doubles damage from a controlled creature with a +1/+1 counter")
    void doublesDamageFromCounteredCreature() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        sorcerer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not double damage from a controlled creature without a +1/+1 counter")
    void doesNotDoubleDamageFromUncounteredCreature() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    void hasteChoiceAllowsImmediateActivationWithoutDoubling() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        harness.castFromHand(player1, new ProdigalSorcerer(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        Permanent sorcerer = findPermanent(player1, "Prodigal Sorcerer");
        assertThat(sorcerer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void multipleCopiesAllowIndependentRiotChoices() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        harness.addToBattlefield(player1, new UncivilUnrest());
        harness.castFromHand(player1, new ProdigalSorcerer(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        Permanent sorcerer = findPermanent(player1, "Prodigal Sorcerer");
        assertThat(sorcerer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.activateAbility(player1, 2, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
    }

    @Test
    void faceDownNontokenCreatureReceivesRiot() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        harness.setHand(player1, List.of(new GrimHaruspex()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        Permanent creature = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(Permanent::isFaceDown).findFirst().orElseThrow();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotGrantRiotToOpponentsCreature() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new ProdigalSorcerer(), "{2}{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        Permanent sorcerer = findPermanent(player2, "Prodigal Sorcerer");
        assertThat(sorcerer.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void doesNotDoubleOpponentsCounteredCreatureDamage() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player2, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        sorcerer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
    }

    @Test
    void doublesDamageToOwnPlayer() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        sorcerer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void doublesDamageToPermanent() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        sorcerer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new ProdigalSorcerer());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Prodigal Sorcerer");
    }

    @Test
    void checksCountersWhenDamageIsDealt() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        sorcerer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.activateAbility(player1, 1, null, player2.getId());
        sorcerer.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 0);
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void otherCounterTypesDoNotDoubleDamage() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        Permanent sorcerer = harness.addToBattlefieldAndReturn(player1, new ProdigalSorcerer());
        sorcerer.setSummoningSick(false);
        sorcerer.setCounterCount(CounterType.CHARGE, 1);

        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    void tokensDoNotReceiveRiotButTheirCounteredDamageIsDoubled() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        Permanent krenko = harness.addToBattlefieldAndReturn(player1, new KrenkoMobBoss());
        krenko.setSummoningSick(false);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().isToken()).toList();
        assertThat(tokens).hasSize(1);
        Permanent goblin = tokens.getFirst();
        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        goblin.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        goblin.setSummoningSick(false);
        goblin.setAttacking(true);

        resolveCombat();

        harness.assertLife(player2, 16);
    }

    @Test
    void multipleRiotCounterChoicesEachAddACounter() {
        harness.addToBattlefield(player1, new UncivilUnrest());
        harness.addToBattlefield(player1, new UncivilUnrest());
        harness.castFromHand(player1, new ProdigalSorcerer(), "{2}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Prodigal Sorcerer")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
