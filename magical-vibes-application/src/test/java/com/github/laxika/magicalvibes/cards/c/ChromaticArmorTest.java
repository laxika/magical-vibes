package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ChromaticArmorTest extends BaseCardTest {

    private static Card createCreature(String name, int power, int toughness, CardColor color) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{1}");
        card.setColor(color);
        card.setPower(power);
        card.setToughness(toughness);
        return card;
    }

    private static Card createDamageInstant(String name, CardColor color, String manaCost, int amount) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.INSTANT);
        card.setManaCost(manaCost);
        card.setColor(color);
        card.addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(amount));
        return card;
    }

    /**
     * Puts a creature on player1's battlefield (index 0) with Chromatic Armor attached (index 1),
     * already carrying {@code sleightCounters} sleight counters and the given chosen color.
     */
    private Permanent addArmoredCreature(CardColor chosen, int sleightCounters) {
        Permanent creature = new Permanent(createCreature("Armored One", 2, 2, CardColor.GREEN));
        creature.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(creature);

        Permanent armor = new Permanent(new ChromaticArmor());
        armor.setAttachedTo(creature.getId());
        armor.setChosenColor(chosen);
        armor.setCounterCount(CounterType.SLEIGHT, sleightCounters);
        gd.playerBattlefields.get(player1.getId()).add(armor);
        return creature;
    }

    private void readyMainPhase() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Enters with a sleight counter and awaits the color choice")
    void entersWithSleightCounterAndColorChoice() {
        Permanent target = new Permanent(createCreature("Grizzly", 2, 2, CardColor.GREEN));
        target.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(target);

        harness.setHand(player1, List.of(new ChromaticArmor()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);
        harness.handleListChoice(player1, "RED");

        Permanent armor = findPermanent(player1, "Chromatic Armor");
        assertThat(armor.getChosenColor()).isEqualTo(CardColor.RED);
        assertThat(armor.getCounterCount(CounterType.SLEIGHT)).isEqualTo(1);
    }

    @Test
    @DisplayName("With one sleight counter the ability costs {1} and adds a counter")
    void abilityCostsOneWithOneCounter() {
        addArmoredCreature(CardColor.RED, 1);
        Permanent armor = findPermanent(player1, "Chromatic Armor");
        readyMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(armor.getCounterCount(CounterType.SLEIGHT)).isEqualTo(2);
        assertThat(armor.getChosenColor()).isEqualTo(CardColor.BLUE);
    }

    @Test
    @DisplayName("Three sleight counters make the ability cost {3}")
    void abilityCostsThreeWithThreeCounters() {
        addArmoredCreature(CardColor.RED, 3);
        Permanent armor = findPermanent(player1, "Chromatic Armor");
        readyMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "GREEN");

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(armor.getCounterCount(CounterType.SLEIGHT)).isEqualTo(4);
        assertThat(armor.getChosenColor()).isEqualTo(CardColor.GREEN);
    }

    @Test
    @DisplayName("Cannot activate with less mana than the number of sleight counters")
    void cannotActivateWithoutEnoughMana() {
        addArmoredCreature(CardColor.RED, 3);
        readyMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Prevents damage from a source of the chosen color")
    void preventsChosenColorDamage() {
        Permanent armored = addArmoredCreature(CardColor.RED, 1);

        harness.setHand(player2, List.of(createDamageInstant("Red Bolt", CardColor.RED, "{R}", 2)));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, armored.getId());
        harness.passBothPriorities();

        assertThat(armored.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(armored.getId()));
    }

    @Test
    @DisplayName("Re-choosing the color moves the prevention to the newly chosen color")
    void rechosenColorShiftsPrevention() {
        Permanent armored = addArmoredCreature(CardColor.RED, 1);
        readyMainPhase();
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleListChoice(player1, "BLUE");

        harness.setHand(player2, List.of(createDamageInstant("Red Bolt", CardColor.RED, "{R}", 2)));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castInstant(player2, 0, armored.getId());
        harness.passBothPriorities();

        // Red is no longer the chosen color, so the red source's damage kills the 2/2.
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(armored.getId()));
    }
}
