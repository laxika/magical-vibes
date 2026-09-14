package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.r.RushingRiver;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SleepingPotion.class, SeaSnidd.class, Singe.class, SlingshotGoblin.class,
        StarCompass.class, RushingRiver.class})
class SleepingPotionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sleeping Potion taps the enchanted creature")
    void resolvingTapsEnchantedCreature() {
        Permanent creature = addCreatureReady(player2, new SeaSnidd());

        harness.setHand(player1, List.of(new SleepingPotion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Sleeping Potion")
                        && p.isAttached()
                        && p.getAttachedTo().equals(creature.getId()));
    }

    @Test
    @DisplayName("Enchanted creature does not untap during its controller's untap step")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2, new SeaSnidd());
        creature.tap();

        attachPotion(player1, creature);

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Enchanted creature untaps after Sleeping Potion leaves the battlefield")
    void creatureUntapsAfterRemoval() {
        Permanent creature = addCreatureReady(player2, new SeaSnidd());
        creature.tap();

        Permanent potion = attachPotion(player1, creature);
        gd.playerBattlefields.get(player1.getId()).remove(potion);

        advanceToUpkeep(player2);

        assertThat(creature.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Sleeping Potion is sacrificed when the enchanted creature becomes the target of a spell")
    void sacrificedWhenEnchantedCreatureTargetedBySpell() {
        Permanent creature = addCreatureReady(player2, new SeaSnidd());
        attachPotion(player1, creature);

        harness.setHand(player1, List.of(new Singe()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, creature.getId());

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sleeping Potion");
        harness.assertInGraveyard(player1, "Sleeping Potion");
    }

    @Test
    @DisplayName("Sleeping Potion is sacrificed when the enchanted creature becomes the target of an ability")
    void sacrificedWhenEnchantedCreatureTargetedByAbility() {
        Permanent creature = addCreatureReady(player2, new SeaSnidd());
        attachPotion(player1, creature);

        Permanent slingshotGoblin = addCreatureReady(player1, new SlingshotGoblin());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(slingshotGoblin), null, creature.getId());

        assertThat(gd.stack).hasSizeGreaterThanOrEqualTo(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Sleeping Potion");
        harness.assertInGraveyard(player1, "Sleeping Potion");
    }

    @Test
    @DisplayName("Sleeping Potion cannot enchant a noncreature permanent")
    void cannotEnchantNonCreature() {
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new StarCompass());

        harness.setHand(player1, List.of(new SleepingPotion()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Targeting Sleeping Potion itself does not trigger its sacrifice ability")
    void notTriggeredWhenPotionItselfIsTargeted() {
        Permanent creature = addCreatureReady(player2, new SeaSnidd());
        Permanent potion = attachPotion(player1, creature);

        harness.setHand(player2, List.of(new RushingRiver()));
        harness.addMana(player2, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0, potion.getId());

        assertThat(gd.stack).hasSize(1);
    }

    private Permanent attachPotion(Player controller, Permanent creature) {
        Permanent potion = new Permanent(new SleepingPotion());
        potion.setAttachedTo(creature.getId());
        gd.playerBattlefields.get(controller.getId()).add(potion);
        return potion;
    }
}
