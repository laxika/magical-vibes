package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
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

class DoorOfDestiniesTest extends BaseCardTest {

    private static Card createCreature(String name, String manaCost, int power, int toughness,
                                       CardColor color, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private static Card createInstant(String name, String manaCost, CardColor color, CardSubtype... subtypes) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.setSubtypes(List.of(subtypes));
        return card;
    }

    private Permanent addDoor(com.github.laxika.magicalvibes.model.Player owner, CardSubtype chosen) {
        Permanent door = new Permanent(new DoorOfDestinies());
        door.setChosenSubtype(chosen);
        gd.playerBattlefields.get(owner.getId()).add(door);
        return door;
    }

    // ===== Entering the battlefield =====

    @Test
    @DisplayName("Resolving Door of Destinies prompts for a creature type choice")
    void castingPromptsForSubtypeChoice() {
        harness.setHand(player1, List.of(new DoorOfDestinies()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
    }

    // ===== Cast trigger: charge counter =====

    @Test
    @DisplayName("Casting a creature spell of the chosen type adds a charge counter")
    void castingChosenTypeCreatureAddsChargeCounter() {
        Permanent door = addDoor(player1, CardSubtype.ELF);

        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF, CardSubtype.DRUID);
        harness.setHand(player1, List.of(elf));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(door.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a non-creature spell of the chosen type also adds a charge counter")
    void castingChosenTypeTribalInstantAddsChargeCounter() {
        Permanent door = addDoor(player1, CardSubtype.GOBLIN);

        Card tribalInstant = createInstant("Tarfire", "{R}", CardColor.RED, CardSubtype.GOBLIN);
        harness.setHand(player1, List.of(tribalInstant));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();

        assertThat(door.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting a spell of a different type does not add a charge counter")
    void castingDifferentTypeDoesNotTrigger() {
        Permanent door = addDoor(player1, CardSubtype.ELF);

        Card goblin = createCreature("Goblin Piker", "{1}{R}", 2, 1, CardColor.RED, CardSubtype.GOBLIN, CardSubtype.WARRIOR);
        harness.setHand(player1, List.of(goblin));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(door.getCounterCount(CounterType.CHARGE)).isEqualTo(0);
    }

    @Test
    @DisplayName("No trigger if no creature type was chosen yet")
    void noTriggerWithoutChoice() {
        harness.addToBattlefield(player1, new DoorOfDestinies());

        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF);
        harness.setHand(player1, List.of(elf));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    // ===== Static boost scaling by charge counters =====

    @Test
    @DisplayName("No boost while Door has no charge counters")
    void noBoostWithoutCounters() {
        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF);
        harness.addToBattlefield(player1, elf);
        addDoor(player1, CardSubtype.ELF);

        Permanent elfPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Creatures of the chosen type get +1/+1 for each charge counter")
    void boostScalesWithChargeCounters() {
        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF, CardSubtype.DRUID);
        harness.addToBattlefield(player1, elf);

        Permanent door = addDoor(player1, CardSubtype.ELF);
        door.setCounterCount(CounterType.CHARGE, 3);

        Permanent elfPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(3);
        assertThat(bonus.toughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Creatures of a different type do not get the boost")
    void doesNotBoostDifferentType() {
        Card goblin = createCreature("Goblin Piker", "{1}{R}", 2, 1, CardColor.RED, CardSubtype.GOBLIN);
        harness.addToBattlefield(player1, goblin);

        Permanent door = addDoor(player1, CardSubtype.ELF);
        door.setCounterCount(CounterType.CHARGE, 3);

        Permanent goblinPerm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Goblin Piker"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, goblinPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent's creatures of the chosen type do not get the boost")
    void doesNotBoostOpponentCreatures() {
        Card elf = createCreature("Llanowar Elves", "{G}", 1, 1, CardColor.GREEN, CardSubtype.ELF);
        harness.addToBattlefield(player2, elf);

        Permanent door = addDoor(player1, CardSubtype.ELF);
        door.setCounterCount(CounterType.CHARGE, 2);

        Permanent elfPerm = gd.playerBattlefields.get(player2.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Llanowar Elves"))
                .findFirst().orElseThrow();

        var bonus = gqs.computeStaticBonus(gd, elfPerm);
        assertThat(bonus.power()).isEqualTo(0);
        assertThat(bonus.toughness()).isEqualTo(0);
    }
}
