package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.b.BramblewoodParagon;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SosukeSonOfSeshiro.class, SkeletalSnake.class, GiantSpider.class, BramblewoodParagon.class, GrizzlyBears.class})
class SosukeSonOfSeshiroTest extends BaseCardTest {

    @Test
    @DisplayName("Other Snake creatures you control get +1/+0")
    void boostsOtherSnakes() {
        addCreatureReady(player1, new SosukeSonOfSeshiro());
        addCreatureReady(player1, new SkeletalSnake());

        Permanent snake = findPermanent(player1, "Skeletal Snake");
        assertThat(gqs.getEffectivePower(gd, snake)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, snake)).isEqualTo(1);
    }

    @Test
    @DisplayName("Sosuke does not boost himself")
    void doesNotBoostItself() {
        addCreatureReady(player1, new SosukeSonOfSeshiro());

        Permanent sosuke = findPermanent(player1, "Sosuke, Son of Seshiro");
        assertThat(gqs.getEffectivePower(gd, sosuke)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sosuke)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost an opponent's Snakes")
    void doesNotBoostOpponentSnakes() {
        addCreatureReady(player1, new SosukeSonOfSeshiro());
        addCreatureReady(player2, new SkeletalSnake());

        Permanent snake = findPermanent(player2, "Skeletal Snake");
        assertThat(gqs.getEffectivePower(gd, snake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, snake)).isEqualTo(1);
    }

    @Test
    @DisplayName("A creature Sosuke damages in combat survives the damage but is destroyed at end of combat")
    void ownCombatDamageDestroysAtEndOfCombat() {
        Permanent sosuke = addCreatureReady(player1, new SosukeSonOfSeshiro());
        sosuke.setAttacking(true);
        addCreatureReady(player2, new GiantSpider()); // 2/4, survives Sosuke's 3 damage

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.passBothPriorities(); // combat damage — Sosuke deals 3 to the Spider
        harness.passBothPriorities(); // resolve the trigger
        harness.passBothPriorities(); // advance through end of combat

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Another Warrior you control triggers the end-of-combat destruction")
    void otherWarriorCombatDamageDestroysAtEndOfCombat() {
        addCreatureReady(player1, new SosukeSonOfSeshiro());
        Permanent paragon = addCreatureReady(player1, new BramblewoodParagon()); // Elf Warrior, not a Snake
        paragon.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        harness.passBothPriorities(); // combat damage — the Warrior deals 2 to the Spider
        harness.passBothPriorities(); // resolve the trigger
        harness.passBothPriorities(); // advance through end of combat

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("A non-Warrior you control does not schedule any destruction")
    void nonWarriorDoesNotSchedule() {
        addCreatureReady(player1, new SosukeSonOfSeshiro());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        harness.passBothPriorities(); // combat damage — Grizzly Bears is not a Warrior
        harness.passBothPriorities();

        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }
}
