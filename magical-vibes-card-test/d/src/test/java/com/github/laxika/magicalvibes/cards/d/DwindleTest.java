package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GreenwoodSentinel;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.cards.s.SimicGuildmage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dwindle.class, GiantSpider.class, GreenwoodSentinel.class, Naturalize.class, SimicGuildmage.class})
class DwindleTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -6/-0")
    void shrinksEnchantedCreature() {
        Permanent spider = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Dwindle()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.forceActivePlayer(player1);

        harness.castEnchantment(player1, 0, spider.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, spider)).isEqualTo(-4);
        assertThat(gqs.getEffectiveToughness(gd, spider)).isEqualTo(4);
    }

    @Test
    @DisplayName("When the enchanted creature blocks, it is destroyed")
    void blockingEnchantedCreatureIsDestroyed() {
        Permanent attacker = addCreatureReady(player1, new GreenwoodSentinel());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        attachDwindle(blocker);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("An unenchanted blocker is not destroyed")
    void unenchantedBlockerSurvives() {
        Permanent attacker = addCreatureReady(player1, new GreenwoodSentinel());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GiantSpider());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("The enchanted creature is not destroyed when it merely attacks")
    void attackingEnchantedCreatureSurvives() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attachDwindle(attacker);
        harness.forceActivePlayer(player1);

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Giant Spider");
    }

    @Test
    @DisplayName("Destroying the blocker does not let a nontrampling attacker deal damage to the player")
    void attackerRemainsBlocked() {
        addCreatureReady(player1, new GreenwoodSentinel());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        attachDwindle(blocker);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        resolveCombat();

        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Removing Dwindle in response does not stop destruction of the blocker")
    void triggerSurvivesAuraRemoval() {
        addCreatureReady(player1, new GreenwoodSentinel());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        Permanent aura = attachDwindle(blocker);
        harness.setHand(player1, List.of(new Naturalize()));
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.castInstant(player1, 0, aura.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Dwindle");
        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Moving Dwindle in response destroys the original blocker rather than the new host")
    void triggerRemembersCreatureThatBlocked() {
        addCreatureReady(player1, new GreenwoodSentinel());
        addCreatureReady(player1, new SimicGuildmage());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        Permanent recipient = addCreatureReady(player2, new GreenwoodSentinel());
        Permanent aura = attachDwindle(blocker);
        declareAttackersAndPrepareBlockers(List.of(0));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.withAutoStop(TurnStep.DECLARE_BLOCKERS,
                () -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))));
        harness.activateAbility(player1, 1, 1, null, aura.getId());
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Giant Spider");
        harness.assertOnBattlefield(player2, "Greenwood Sentinel");
        assertThat(aura.getAttachedTo()).isEqualTo(recipient.getId());
    }

    /**
     * Puts a Dwindle onto the battlefield attached to the given creature, under that
     * creature's controller.
     */
    private Permanent attachDwindle(Permanent host) {
        Permanent aura = harness.addToBattlefieldAndReturn(
                gd.findControllerOf(host).equals(player1.getId()) ? player1 : player2, new Dwindle());
        aura.setAttachedTo(host.getId());
        return aura;
    }
}
