package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EternalThirst.class, GrizzlyBears.class, Ornithopter.class, Shock.class,
        RuneclawBear.class, LightningStrike.class, Naturalize.class})
class EternalThirstTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature has lifelink")
    void enchantedCreatureHasLifelink() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, creature);
        int startingLife = gd.playerLifeTotals.get(player1.getId());

        creature.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife + 2);
    }

    @Test
    @DisplayName("Enchanted creature gets a counter when an opponent's creature dies")
    void enchantedCreatureGetsCounterWhenOpponentsCreatureDies() {
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        attachAura(player1, creature);
        Permanent dyingCreature = addCreatureReady(player1, new Ornithopter());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, dyingCreature.getId());
        harness.passBothPriorities();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Enchanted creature does not get a counter when its controller's creature dies")
    void enchantedCreatureDoesNotGetCounterWhenItsOwnCreatureDies() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        attachAura(player1, creature);
        Permanent dyingCreature = addCreatureReady(player1, new Ornithopter());

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, dyingCreature.getId());

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Casting the Aura on an opponent's creature gives lifelink to that creature's controller")
    void castingAuraOnOpponentsCreatureGrantsLifelink() {
        Permanent creature = addCreatureReady(player2, new RuneclawBear());
        harness.setHand(player1, List.of(new EternalThirst()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent aura = findPermanent(player1, "Eternal Thirst");
        assertThat(aura.getAttachedTo()).isEqualTo(creature.getId());
        int controllerLife = gd.playerLifeTotals.get(player2.getId());
        int auraControllerLife = gd.playerLifeTotals.get(player1.getId());
        creature.setAttacking(true);
        resolveCombat(player2);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(controllerLife + 2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(auraControllerLife - 2);
    }

    @Test
    @DisplayName("Each opposing creature death grants a separate counter")
    void separateDeathsEachGrantCounter() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        attachAura(player1, creature);
        Permanent first = addCreatureReady(player2, new Ornithopter());
        Permanent second = addCreatureReady(player2, new Ornithopter());
        harness.setHand(player1, List.of(new LightningStrike(), new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castAndResolveInstant(player1, 0, first.getId());
        resolveAllTriggers();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.castAndResolveInstant(player1, 0, second.getId());
        resolveAllTriggers();

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removing the Aura removes both granted abilities")
    void removingAuraRemovesGrantedAbilities() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        attachAura(player1, creature);
        Permanent victim = addCreatureReady(player2, new Ornithopter());
        harness.setHand(player1, List.of(new Naturalize(), new LightningStrike()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Eternal Thirst").getId());
        harness.assertInGraveyard(player1, "Eternal Thirst");
        harness.castAndResolveInstant(player1, 0, victim.getId());
        resolveAllTriggers();
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        int startingLife = gd.playerLifeTotals.get(player1.getId());
        creature.setAttacking(true);
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(startingLife);
    }

    @Test
    @DisplayName("A pending counter ability still resolves after the Aura is destroyed")
    void pendingCounterAbilitySurvivesAuraRemoval() {
        Permanent creature = addCreatureReady(player1, new RuneclawBear());
        attachAura(player1, creature);
        Permanent victim = addCreatureReady(player2, new Ornithopter());
        harness.setHand(player1, List.of(new LightningStrike(), new Naturalize()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castAndResolveInstant(player1, 0, victim.getId());
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        harness.castAndResolveInstant(player1, 0, findPermanent(player1, "Eternal Thirst").getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Eternal Thirst");
        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void attachAura(Player auraController, Permanent creature) {
        Permanent aura = new Permanent(new EternalThirst());
        aura.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(auraController.getId()).add(aura);
    }
}
