package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.o.OminousParcel;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
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

@CardUsed({SleepWithTheFishes.class, SewerCrocodile.class, OminousParcel.class})
class SleepWithTheFishesTest extends BaseCardTest {

    @Test
    @DisplayName("Entering Sleep with the Fishes taps the creature and creates an unblockable Fish")
    void entersTapsCreatureAndCreatesFish() {
        Permanent creature = addCreatureReady(player2);

        castSleepWithTheFishes(creature);

        assertThat(creature.isTapped()).isTrue();
        Permanent fish = findPermanent(player1, "Fish");
        assertThat(fish.getCard().getColor()).isEqualTo(CardColor.BLUE);
        assertThat(fish.getCard().getSubtypes()).containsExactly(CardSubtype.FISH);
        assertThat(gqs.getEffectivePower(gd, fish)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, fish)).isEqualTo(1);
        assertThat(gqs.hasCantBeBlocked(gd, fish)).isTrue();
    }

    @Test
    @DisplayName("The enchanted creature does not untap while Sleep with the Fishes remains attached")
    void enchantedCreatureDoesNotUntap() {
        Permanent creature = addCreatureReady(player2);

        castSleepWithTheFishes(creature);
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Sleep with the Fishes cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new OminousParcel());
        harness.setHand(player1, List.of(new SleepWithTheFishes()));
        addMana();

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    void alreadyTappedCreatureStillCreatesExactlyOneFish() {
        Permanent creature = addCreatureReady(player2);
        creature.setTapped(true);

        castSleepWithTheFishes(creature);

        assertThat(creature.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Fish"))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Fish"));
    }

    @Test
    void creatureUntapsAfterAuraLeavesButFishRemains() {
        Permanent creature = addCreatureReady(player2);
        castSleepWithTheFishes(creature);
        Permanent aura = findPermanent(player1, "Sleep with the Fishes");

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura);
        harness.performUntapStep(player2);

        assertThat(creature.isTapped()).isFalse();
        harness.assertOnBattlefield(player1, "Fish");
    }

    @Test
    void triggerStillTapsCreatureAndCreatesFishAfterAuraLeaves() {
        Permanent creature = addCreatureReady(player2);
        harness.setHand(player1, List.of(new SleepWithTheFishes()));
        addMana();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();
        assertThat(creature.isTapped()).isFalse();
        harness.assertNotOnBattlefield(player1, "Fish");

        Permanent aura = findPermanent(player1, "Sleep with the Fishes");
        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, aura);
        resolveAllTriggers();

        assertThat(creature.isTapped()).isTrue();
        harness.assertOnBattlefield(player1, "Fish");
    }

    @Test
    void triggerStillCreatesFishAfterEnchantedCreatureLeaves() {
        Permanent creature = addCreatureReady(player2);
        harness.setHand(player1, List.of(new SleepWithTheFishes()));
        addMana();
        harness.castEnchantment(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.getPermanentRemovalService().removePermanentToGraveyard(gd, creature);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Fish");
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(creature);
    }

    private Permanent addCreatureReady(Player player) {
        Permanent creature = harness.addToBattlefieldAndReturn(player, new SewerCrocodile());
        creature.setSummoningSick(false);
        return creature;
    }

    private void castSleepWithTheFishes(Permanent target) {
        harness.setHand(player1, List.of(new SleepWithTheFishes()));
        addMana();
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        resolveAllTriggers();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }

}
