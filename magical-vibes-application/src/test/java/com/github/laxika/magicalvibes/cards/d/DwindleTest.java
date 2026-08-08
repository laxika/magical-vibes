package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DwindleTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets -6/-0")
    void shrinksEnchantedCreature() {
        harness.addToBattlefield(player2, new GiantSpider()); // 2/4
        Permanent spider = findPermanent(player2, "Giant Spider");
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
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
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
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
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

    /**
     * Puts a Dwindle onto the battlefield attached to the given creature, under that
     * creature's controller.
     */
    private void attachDwindle(Permanent host) {
        Permanent aura = new Permanent(new Dwindle());
        aura.setAttachedTo(host.getId());
        gd.playerBattlefields.get(gd.findControllerOf(host)).add(aura);
    }
}
