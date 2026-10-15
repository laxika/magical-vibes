package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BeanstalkGiant;
import com.github.laxika.magicalvibes.cards.f.FertileFootsteps;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StartingTownNpc.class, GrizzlyBears.class, BeanstalkGiant.class, FertileFootsteps.class})
class StartingTownNpcTest extends BaseCardTest {

    @Test
    void grantsFetchHerbsAdventureToCreatureCardsInHand() {
        harness.addToBattlefield(player1, new StartingTownNpc());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(22);
        assertThat(gd.findExiledCard(bears.getId())).isNotNull();
    }

    @Test
    void creatureCastFromExileEntersWithAnAdditionalCounter() {
        harness.addToBattlefield(player1, new StartingTownNpc());
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, bears.getId());
        resolveAllTriggers();

        Permanent enteredBears = findPermanent(player1, "Grizzly Bears");
        assertThat(enteredBears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void doesNotCreateACastTriggerForTheEntryCounter() {
        harness.addToBattlefield(player1, new StartingTownNpc());
        StartingTownNpc creature = new StartingTownNpc();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.castFromExile(player1, creature.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void doesNotGrantCounterWhenSourceLeavesBeforeCreatureResolves() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new StartingTownNpc());
        StartingTownNpc creature = new StartingTownNpc();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.castFromExile(player1, creature.getId());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));

        resolveAllTriggers();

        assertThat(findPermanent(player1, "Starting Town NPC")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void grantsCounterWhenSourceEntersAfterCreatureWasCast() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new StartingTownNpc());
        StartingTownNpc creature = new StartingTownNpc();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));
        harness.castFromExile(player1, creature.getId());
        harness.addToBattlefield(player1, new StartingTownNpc());

        resolveAllTriggers();

        Permanent entered = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(creature.getId()))
                .findFirst().orElseThrow();
        assertThat(entered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void adventureStillExilesAndAllowsCastingAfterSourceLeavesWhileItIsOnStack() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new StartingTownNpc());
        StartingTownNpc creature = new StartingTownNpc();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAdventure(player1, 0, List.of());
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, source));

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
        harness.castFromExile(player1, creature.getId());
        resolveAllTriggers();
        harness.assertOnBattlefield(player1, "Starting Town NPC");
        assertThat(findPermanent(player1, "Starting Town NPC")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void creatureCastFromHandDoesNotReceiveAnEntryCounter() {
        harness.addToBattlefield(player1, new StartingTownNpc());
        StartingTownNpc creature = new StartingTownNpc();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Starting Town NPC"))
                .allSatisfy(permanent -> assertThat(
                        permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero());
        assertThat(countPermanents(player1, "Starting Town NPC")).isEqualTo(2);
    }

    @Test
    @CardUsed({StartingTownNpc.class, BeanstalkGiant.class, FertileFootsteps.class})
    void grantsFetchHerbsEvenWhenCreatureAlreadyHasAnAdventure() {
        harness.addToBattlefield(player1, new StartingTownNpc());
        BeanstalkGiant creature = new BeanstalkGiant();
        harness.setHand(player1, List.of(creature));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAdventure(player1, 0, List.of());
        harness.passBothPriorities();

        harness.assertLife(player1, 22);
        assertThat(gd.findExiledCard(creature.getId())).isNotNull();
    }
}
