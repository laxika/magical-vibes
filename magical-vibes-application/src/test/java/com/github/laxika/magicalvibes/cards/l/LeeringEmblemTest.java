package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JacesIngenuity;
import com.github.laxika.magicalvibes.cards.d.DuskdaleWurm;
import com.github.laxika.magicalvibes.cards.o.OonasGrace;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LeeringEmblem.class, GrizzlyBears.class, JacesIngenuity.class,
        DuskdaleWurm.class, OonasGrace.class})
class LeeringEmblemTest extends BaseCardTest {

    @Test
    @DisplayName("Casting a spell gives the equipped creature +2/+2")
    void castingSpellBoostsEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent emblem = addCreatureReady(player1, new LeeringEmblem());
        emblem.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castAndResolveInstant(player1, 0);

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent emblem = addCreatureReady(player1, new LeeringEmblem());
        emblem.setAttachedTo(creature.getId());

        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castAndResolveInstant(player1, 0);

        assertThat(creature.getPowerModifier()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("The ability triggers while unattached but boosts no creature")
    void triggerWhileUnattachedBoostsNoCreature() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new LeeringEmblem()); // present but unattached

        harness.setHand(player1, List.of(new JacesIngenuity()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castInstant(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(0);
        assertThat(creature.getToughnessModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Resolving equip attaches the Equipment to the target creature")
    void resolvingEquipAttaches() {
        Permanent emblem = addCreatureReady(player1, new LeeringEmblem());
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(emblem.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Creature spells trigger the boost before the creature resolves")
    void creatureSpellTriggersBoost() {
        Permanent creature = addCreatureReady(player1, new DuskdaleWurm());
        Permanent emblem = addCreatureReady(player1, new LeeringEmblem());
        emblem.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new DuskdaleWurm()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(creature.getPowerModifier()).isEqualTo(2);
        assertThat(creature.getToughnessModifier()).isEqualTo(2);
        assertThat(countPermanents(player1, "Duskdale Wurm")).isEqualTo(1);
    }

    @Test
    @DisplayName("Multiple spells give cumulative boosts")
    void multipleSpellsGiveCumulativeBoosts() {
        Permanent creature = addCreatureReady(player1, new DuskdaleWurm());
        Permanent emblem = addCreatureReady(player1, new LeeringEmblem());
        emblem.setAttachedTo(creature.getId());
        harness.setHand(player1, List.of(new OonasGrace(), new OonasGrace()));
        harness.setLibrary(player1, List.of(new DuskdaleWurm(), new DuskdaleWurm()));
        harness.addMana(player1, ManaColor.BLUE, 6);

        harness.castInstant(player1, 0, player1.getId());
        harness.castInstant(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(creature.getPowerModifier()).isEqualTo(4);
        assertThat(creature.getToughnessModifier()).isEqualTo(4);
    }

    @Test
    @DisplayName("Moving the Equipment after the trigger resolves leaves the boost on the old creature")
    void movingEquipmentDoesNotMoveResolvedBoost() {
        Permanent emblem = addCreatureReady(player1, new LeeringEmblem());
        Permanent originalCreature = addCreatureReady(player1, new DuskdaleWurm());
        Permanent nextCreature = addCreatureReady(player1, new DuskdaleWurm());
        emblem.setAttachedTo(originalCreature.getId());
        harness.setHand(player1, List.of(new OonasGrace()));
        harness.setLibrary(player1, List.of(new DuskdaleWurm()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 0, null, nextCreature.getId());
        harness.castInstant(player1, 0, player1.getId());
        resolveAllTriggers();

        assertThat(emblem.getAttachedTo()).isEqualTo(nextCreature.getId());
        assertThat(originalCreature.getPowerModifier()).isEqualTo(2);
        assertThat(originalCreature.getToughnessModifier()).isEqualTo(2);
        assertThat(nextCreature.getPowerModifier()).isZero();
        assertThat(nextCreature.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("An opponent's spell does not trigger the Equipment")
    void opponentsSpellDoesNotTrigger() {
        Permanent creature = addCreatureReady(player1, new DuskdaleWurm());
        Permanent emblem = addCreatureReady(player1, new LeeringEmblem());
        emblem.setAttachedTo(creature.getId());
        harness.setHand(player2, List.of(new OonasGrace()));
        harness.setLibrary(player2, List.of(new DuskdaleWurm()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castInstant(player2, 0, player2.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(creature.getPowerModifier()).isZero();
        assertThat(creature.getToughnessModifier()).isZero();
    }
}
