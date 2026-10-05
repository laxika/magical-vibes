package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.FaithbearerPaladin;
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

@CardUsed({NoosegrafMob.class, FaithbearerPaladin.class})
class NoosegrafMobTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with five +1/+1 counters")
    void entersWithFiveCounters() {
        harness.setHand(player1, List.of(new NoosegrafMob()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent mob = findPermanent(player1, "Noosegraf Mob");
        assertThat(mob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    @DisplayName("A player casting a spell removes one counter and creates a Zombie for the controller")
    void anyPlayerCastingSpellCreatesZombie() {
        Permanent mob = addReadyMob(player1, 5);
        harness.setHand(player2, List.of(new FaithbearerPaladin()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        resolveAllTriggers();

        assertThat(mob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(findPermanents(player2, "Zombie")).isEmpty();
    }

    @Test
    @DisplayName("Removing the last counter still creates a Zombie")
    void lastCounterStillCreatesZombie() {
        addReadyMob(player1, 1);
        harness.setHand(player1, List.of(new FaithbearerPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Noosegraf Mob")).isEmpty();
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
    }

    @Test
    @DisplayName("The Zombie is created in the same resolution that removes the counter")
    void zombieCreationDoesNotUseASeparateTrigger() {
        Permanent mob = addReadyMob(player1, 5);
        harness.setHand(player1, List.of(new FaithbearerPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(mob.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(findPermanents(player1, "Zombie")).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        assertThat(findPermanents(player1, "Faithbearer Paladin")).isEmpty();
        resolveAllTriggers();
    }

    @Test
    @DisplayName("No Zombie is created if the Mob has left before its trigger resolves")
    void absentSourceCannotCreateZombie() {
        Permanent mob = addReadyMob(player1, 5);
        harness.setHand(player1, List.of(new FaithbearerPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        gd.playerBattlefields.get(player1.getId()).remove(mob);
        gd.playerGraveyards.get(player1.getId()).add(mob.getCard());
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Zombie")).isEmpty();
        harness.assertInGraveyard(player1, "Noosegraf Mob");
    }

    private Permanent addReadyMob(Player player, int counterCount) {
        Permanent mob = addCreatureReady(player, new NoosegrafMob());
        mob.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counterCount);
        return mob;
    }
}
