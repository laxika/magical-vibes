package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DisciplesOfTheInferno;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RalsReinforcements;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DisciplesOfTheInferno.class, GrizzlyBears.class, InvasionOfRegatha.class,
        RalsReinforcements.class, Shock.class})
class InvasionOfRegathaTest extends BaseCardTest {

    @Test
    void entersAndDealsDamageToAnotherBattleAndCreature() {
        Permanent otherBattle = harness.addToBattlefieldAndReturn(player2, new InvasionOfRegatha());
        otherBattle.setCounterCount(CounterType.DEFENSE, 5);
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new InvasionOfRegatha()));
        addInvasionMana();
        harness.castSorcery(player1, 0, List.of(otherBattle.getId(), creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(otherBattle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
        assertThat(creature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    void entersAndDealsFourDamageToAnOpponentWithoutCreatureTarget() {
        harness.setHand(player1, List.of(new InvasionOfRegatha()));
        addInvasionMana();
        harness.setLife(player2, 20);

        harness.castSorcery(player1, 0, List.of(player2.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void defeatingTheSiegeCastsTheCreatureBackFace() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfRegatha());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent transformed = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.isTransformed())
                .findFirst()
                .orElseThrow();
        assertThat(transformed.getCard()).isInstanceOf(DisciplesOfTheInferno.class);
    }

    @Test
    void backFaceAddsTwoDamageFromNoncreatureSourcesToCreatures() {
        harness.addToBattlefield(player1, new InvasionOfRegatha().getBackFaceCard());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    @Test
    void backFaceAddsTwoDamageFromNoncreatureSourcesToOpponents() {
        harness.addToBattlefield(player1, new InvasionOfRegatha().getBackFaceCard());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player2.getId())).isEqualTo(16);
    }

    @Test
    void backFaceProwessTriggersForControllerNoncreatureSpell() {
        Permanent disciples = harness.addToBattlefieldAndReturn(player1,
                new InvasionOfRegatha().getBackFaceCard());

        harness.castFromHand(player1, new RalsReinforcements(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, disciples)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, disciples)).isEqualTo(5);
        harness.passBothPriorities();
    }

    @Test
    void backFaceProwessDoesNotTriggerForCreatureSpell() {
        Permanent disciples = harness.addToBattlefieldAndReturn(player1,
                new InvasionOfRegatha().getBackFaceCard());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, disciples)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, disciples)).isEqualTo(4);
    }

    @Test
    void backFaceAddsExactlyTwoToTheSiegesCreatureDamage() {
        harness.addToBattlefield(player1, new InvasionOfRegatha().getBackFaceCard());
        Permanent creature = harness.addToBattlefieldAndReturn(player2,
                new InvasionOfRegatha().getBackFaceCard());
        harness.setHand(player1, List.of(new InvasionOfRegatha()));
        addInvasionMana();

        harness.castSorcery(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(creature);
        harness.assertLife(player2, 14);
    }

    @Test
    void backFaceAddsTwoDamageToBattlesEvenWhenControllerOwnsTheBattle() {
        harness.addToBattlefield(player1, new InvasionOfRegatha().getBackFaceCard());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfRegatha());
        battle.setCounterCount(CounterType.DEFENSE, 5);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, battle.getId());
        harness.passBothPriorities();

        assertThat(battle.getCounterCount(CounterType.DEFENSE)).isEqualTo(1);
    }

    @Test
    void backFaceDoesNotIncreaseDamageToItsController() {
        harness.addToBattlefield(player1, new InvasionOfRegatha().getBackFaceCard());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
    }

    @Test
    void backFaceDoesNotIncreaseOpponentsDamageOrTriggerForTheirSpells() {
        Permanent disciples = harness.addToBattlefieldAndReturn(player1,
                new InvasionOfRegatha().getBackFaceCard());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, disciples.getId());

        assertThat(disciples.getMarkedDamage()).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, disciples)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, disciples)).isEqualTo(4);
    }

    @Test
    void controllerCanDeclineCastingTheDefeatedSiege() {
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfRegatha());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(battle.getCard().getId())).isNotNull();
        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Disciples of the Inferno");
    }

    @Test
    void castingTheDefeatedSiegeAsACreatureDoesNotTriggerProwess() {
        Permanent disciples = harness.addToBattlefieldAndReturn(player1,
                new InvasionOfRegatha().getBackFaceCard());
        Permanent battle = harness.addToBattlefieldAndReturn(player1, new InvasionOfRegatha());
        battle.setCounterCount(CounterType.DEFENSE, 0);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(BattleDefeatSupport.class)
                .checkAfterDefenseRemoved(gd, battle));

        harness.passBothPriorities();
        if (gd.interaction.isAwaitingInput()) {
            harness.handleMayAbilityChosen(player1, true);
        }
        harness.passBothPriorities();
        if (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        assertThat(gqs.getEffectivePower(gd, disciples)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, disciples)).isEqualTo(4);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard() instanceof DisciplesOfTheInferno)
                .hasSize(2);
    }

    @Test
    void fourDamageCannotTargetTheController() {
        harness.setHand(player1, List.of(new InvasionOfRegatha()));
        addInvasionMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void fourDamageStillResolvesWhenTheCreatureTargetDies() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new InvasionOfRegatha(), new Shock()));
        addInvasionMana();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, List.of(player2.getId(), creature.getId()));
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    private void addInvasionMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
