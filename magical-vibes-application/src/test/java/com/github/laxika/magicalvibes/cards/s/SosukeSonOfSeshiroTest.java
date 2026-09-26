package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.h.HumbleBudoka;
import com.github.laxika.magicalvibes.cards.k.KamiOfOldStone;
import com.github.laxika.magicalvibes.cards.o.OrochiSustainer;
import com.github.laxika.magicalvibes.cards.s.SokenzanBruiser;
import com.github.laxika.magicalvibes.cards.v.ViridianLongbow;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.action.DelayedPermanentAction;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SosukeSonOfSeshiro.class, OrochiSustainer.class, KamiOfOldStone.class,
        SokenzanBruiser.class, HumbleBudoka.class, ViridianLongbow.class})
class SosukeSonOfSeshiroTest extends BaseCardTest {

    @Test
    @DisplayName("Other Snake creatures you control get +1/+0")
    void boostsOtherSnakes() {
        addCreatureReady(player1, new SosukeSonOfSeshiro());
        addCreatureReady(player1, new OrochiSustainer());

        Permanent snake = findPermanent(player1, "Orochi Sustainer");
        assertThat(gqs.getEffectivePower(gd, snake)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, snake)).isEqualTo(2);
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
        addCreatureReady(player2, new OrochiSustainer());

        Permanent snake = findPermanent(player2, "Orochi Sustainer");
        assertThat(gqs.getEffectivePower(gd, snake)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, snake)).isEqualTo(2);
    }

    @Test
    @DisplayName("A creature Sosuke damages in combat survives the damage but is destroyed at end of combat")
    void ownCombatDamageDestroysAtEndOfCombat() {
        Permanent sosuke = addCreatureReady(player1, new SosukeSonOfSeshiro());
        sosuke.setAttacking(true);
        addCreatureReady(player2, new KamiOfOldStone()); // 1/7, survives Sosuke's 3 damage

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(); // combat damage — Sosuke deals 3 to Kami of Old Stone
        harness.assertOnBattlefield(player2, "Kami of Old Stone");
        harness.passBothPriorities(); // resolve the trigger
        harness.passBothPriorities(); // advance through end of combat

        harness.assertNotOnBattlefield(player2, "Kami of Old Stone");
        harness.assertInGraveyard(player2, "Kami of Old Stone");
    }

    @Test
    @DisplayName("Another Warrior you control triggers the end-of-combat destruction")
    void otherWarriorCombatDamageDestroysAtEndOfCombat() {
        addCreatureReady(player1, new SosukeSonOfSeshiro());
        Permanent warrior = addCreatureReady(player1, new SokenzanBruiser()); // Ogre Warrior, not a Snake
        warrior.setAttacking(true);
        addCreatureReady(player2, new KamiOfOldStone()); // 1/7, survives Sokenzan Bruiser's 3 damage

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        resolveCombat(); // combat damage — the Warrior deals 3 to Kami of Old Stone
        harness.passBothPriorities(); // resolve the trigger
        harness.passBothPriorities(); // advance through end of combat

        harness.assertNotOnBattlefield(player2, "Kami of Old Stone");
        harness.assertInGraveyard(player2, "Kami of Old Stone");
    }

    @Test
    @DisplayName("A non-Warrior you control does not schedule any destruction")
    void nonWarriorDoesNotSchedule() {
        addCreatureReady(player1, new SosukeSonOfSeshiro());
        Permanent nonWarrior = addCreatureReady(player1, new HumbleBudoka());
        nonWarrior.setAttacking(true);
        addCreatureReady(player2, new KamiOfOldStone());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        resolveCombat(); // combat damage — Humble Budoka is not a Warrior

        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
    }

    @Test
    @DisplayName("An opponent's Warrior does not trigger Sosuke")
    void opponentWarriorDoesNotTrigger() {
        addCreatureReady(player1, new SosukeSonOfSeshiro());
        addCreatureReady(player1, new KamiOfOldStone());
        Permanent opponentWarrior = addCreatureReady(player2, new SokenzanBruiser());
        opponentWarrior.setAttacking(true);

        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));

        resolveCombat(player2);

        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
        harness.assertOnBattlefield(player1, "Kami of Old Stone");
    }

    @Test
    @DisplayName("Noncombat damage from a Warrior does not trigger Sosuke")
    void noncombatWarriorDamageDoesNotTrigger() {
        addCreatureReady(player1, new SosukeSonOfSeshiro());
        Permanent warrior = addCreatureReady(player1, new SokenzanBruiser());
        Permanent longbow = addCreatureReady(player1, new ViridianLongbow());
        longbow.setAttachedTo(warrior.getId());
        addCreatureReady(player2, new KamiOfOldStone());

        UUID targetId = harness.getPermanentId(player2, "Kami of Old Stone");
        harness.activateAbility(player1, 1, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(gd.hasDelayedAction(DelayedPermanentAction.class)).isFalse();
        harness.assertOnBattlefield(player2, "Kami of Old Stone");
    }
}
