package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.b.BorealDruid;
import com.github.laxika.magicalvibes.cards.m.MishrasBauble;
import com.github.laxika.magicalvibes.cards.s.SurgingAether;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KrovikanWhispers.class, BorealDruid.class, MishrasBauble.class, SurgingAether.class})
class KrovikanWhispersTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Krovikan Whispers steals the enchanted creature")
    void resolvingStealsCreature() {
        Permanent creature = addCreatureReady(player2, new BorealDruid());

        harness.setHand(player1, List.of(new KrovikanWhispers()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Boreal Druid");
        harness.assertNotOnBattlefield(player2, "Boreal Druid");
    }

    @Test
    @DisplayName("Cumulative upkeep can be paid with black mana")
    void cumulativeUpkeepAcceptsBlackMana() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        harness.setHand(player1, List.of(new KrovikanWhispers()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent whispers = findPermanent(player1, "Krovikan Whispers");
        whispers.setCounterCount(CounterType.AGE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(whispers.getCounterCount(CounterType.AGE)).isEqualTo(2);

        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(whispers);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices the Aura and loses life for its age counters")
    void decliningUpkeepSacrificesAndLosesLife() {
        Permanent creature = addCreatureReady(player1, new BorealDruid());
        harness.setHand(player1, List.of(new KrovikanWhispers()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent whispers = findPermanent(player1, "Krovikan Whispers");
        whispers.setCounterCount(CounterType.AGE, 2);
        harness.setLife(player1, 20);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        assertThat(whispers.getCounterCount(CounterType.AGE)).isEqualTo(3);

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(whispers);
        harness.assertInGraveyard(player1, "Krovikan Whispers");
        harness.assertLife(player1, 14);
    }

    @Test
    @DisplayName("Returning the Aura to hand does not cause its graveyard ability")
    void returningAuraToHandDoesNotLoseLife() {
        Permanent creature = addCreatureReady(player2, new BorealDruid());
        harness.setHand(player1, List.of(new KrovikanWhispers()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        Permanent whispers = findPermanent(player1, "Krovikan Whispers");
        whispers.setCounterCount(CounterType.AGE, 2);
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new SurgingAether()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castInstant(player1, 0, whispers.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Krovikan Whispers");
        harness.assertInHand(player1, "Krovikan Whispers");
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Krovikan Whispers cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new MishrasBauble());
        harness.setHand(player1, List.of(new KrovikanWhispers()));
        harness.addMana(player1, ManaColor.BLUE, 4);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
