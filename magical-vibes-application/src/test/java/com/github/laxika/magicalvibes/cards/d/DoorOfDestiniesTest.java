package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.b.BramblewoodParagon;
import com.github.laxika.magicalvibes.cards.b.BladesOfVelisVel;
import com.github.laxika.magicalvibes.cards.h.HuntingTriad;
import com.github.laxika.magicalvibes.cards.m.MoongloveChangeling;
import com.github.laxika.magicalvibes.cards.m.MudbuttonClanger;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DoorOfDestinies.class, BramblewoodParagon.class, HuntingTriad.class,
        MudbuttonClanger.class, MoongloveChangeling.class, BladesOfVelisVel.class})
class DoorOfDestiniesTest extends BaseCardTest {

    private Permanent addDoor(com.github.laxika.magicalvibes.model.Player owner, CardSubtype chosen) {
        Permanent door = harness.addToBattlefieldAndReturn(owner, new DoorOfDestinies());
        door.setChosenSubtype(chosen);
        return door;
    }

    @Test
    @DisplayName("Resolving Door of Destinies prompts for a creature type choice")
    void castingPromptsForSubtypeChoice() {
        harness.setHand(player1, List.of(new DoorOfDestinies()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    @Test
    @DisplayName("Casting a creature spell of the chosen type adds a charge counter")
    void castingChosenTypeCreatureAddsChargeCounter() {
        Permanent door = addDoor(player1, CardSubtype.ELF);

        Card elf = new BramblewoodParagon();
        harness.setHand(player1, List.of(elf));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(door.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a kindred sorcery of the chosen type also adds a charge counter")
    void castingChosenTypeKindredSorceryAddsChargeCounter() {
        Permanent door = addDoor(player1, CardSubtype.ELF);

        Card kindredSorcery = new HuntingTriad();
        harness.setHand(player1, List.of(kindredSorcery));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(door.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell of a different type does not add a charge counter")
    void castingDifferentTypeDoesNotTrigger() {
        Permanent door = addDoor(player1, CardSubtype.ELF);

        Card goblin = new MudbuttonClanger();
        harness.setHand(player1, List.of(goblin));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(door.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("No trigger if no creature type was chosen yet")
    void noTriggerWithoutChoice() {
        harness.addToBattlefield(player1, new DoorOfDestinies());

        Card elf = new BramblewoodParagon();
        harness.setHand(player1, List.of(elf));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("No boost while Door has no charge counters")
    void noBoostWithoutCounters() {
        Permanent elfPerm = harness.addToBattlefieldAndReturn(player1, new BramblewoodParagon());
        addDoor(player1, CardSubtype.ELF);

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Creatures of the chosen type get +1/+1 for each charge counter")
    void boostScalesWithChargeCounters() {
        Permanent elfPerm = harness.addToBattlefieldAndReturn(player1, new BramblewoodParagon());

        Permanent door = addDoor(player1, CardSubtype.ELF);
        door.setCounterCount(CounterType.CHARGE, 3);

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(3);
        assertThat(bonus.toughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures of a different type do not get the boost")
    void doesNotBoostDifferentType() {
        Permanent goblinPerm = harness.addToBattlefieldAndReturn(player1, new MudbuttonClanger());

        Permanent door = addDoor(player1, CardSubtype.ELF);
        door.setCounterCount(CounterType.CHARGE, 3);

        var bonus = gqs.computeStaticBonus(gd, goblinPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's creatures of the chosen type do not get the boost")
    void doesNotBoostOpponentCreatures() {
        Permanent elfPerm = harness.addToBattlefieldAndReturn(player2, new BramblewoodParagon());

        Permanent door = addDoor(player1, CardSubtype.ELF);
        door.setCounterCount(CounterType.CHARGE, 2);

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    void chosenTypeIsRememberedAfterResolution() {
        harness.setHand(player1, List.of(new DoorOfDestinies()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.handleListChoice(player1, CardSubtype.ELF.name());

        Permanent door = findPermanent(player1, "Door of Destinies");
        assertThat(door.getChosenSubtype()).isEqualTo(CardSubtype.ELF);
        assertThat(door.getCounterCount(CounterType.CHARGE)).isZero();
        harness.setHand(player1, List.of(new BramblewoodParagon()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(door.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.assertNotOnBattlefield(player1, "Bramblewood Paragon");
        harness.passBothPriorities();
        Permanent elf = findPermanent(player1, "Bramblewood Paragon");
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(3);
    }

    @Test
    void opponentCastingChosenTypeDoesNotTrigger() {
        Permanent door = addDoor(player1, CardSubtype.ELF);
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new BramblewoodParagon()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(door.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    void changelingTriggersAndReceivesBoost() {
        Permanent door = addDoor(player1, CardSubtype.ELF);
        harness.setHand(player1, List.of(new MoongloveChangeling()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(door.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        harness.passBothPriorities();
        Permanent changeling = findPermanent(player1, "Moonglove Changeling");
        assertThat(gqs.getEffectivePower(gd, changeling)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, changeling)).isEqualTo(3);
    }

    @Test
    void enteringWithoutCastingDoesNotAddCounter() {
        Permanent door = addDoor(player1, CardSubtype.ELF);
        harness.enterBattlefieldAndReturn(player1, new BramblewoodParagon());
        assertThat(door.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void onlyChargeCountersContributeAndRemovingDoorEndsBoost() {
        Permanent elf = harness.addToBattlefieldAndReturn(player1, new BramblewoodParagon());
        Permanent door = addDoor(player1, CardSubtype.ELF);
        door.setCounterCount(CounterType.CHARGE, 2);
        door.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(4);
        gd.playerBattlefields.get(player1.getId()).remove(door);
        assertThat(gqs.getEffectivePower(gd, elf)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, elf)).isEqualTo(2);
    }

    @Test
    void kindredChangelingSpellTriggersForChosenType() {
        Permanent door = addDoor(player1, CardSubtype.ELF);
        harness.setHand(player1, List.of(new BladesOfVelisVel()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, List.of());

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(door.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }
}
