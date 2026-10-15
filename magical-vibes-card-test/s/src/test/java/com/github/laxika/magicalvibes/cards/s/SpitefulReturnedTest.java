package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FelhideBrawler;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpitefulReturned.class, FelhideBrawler.class})
class SpitefulReturnedTest extends BaseCardTest {

    @Test
    @DisplayName("Spiteful Returned makes the defending player lose 2 life when it attacks")
    void creatureAttackMakesDefendingPlayerLoseLife() {
        addCreatureReady(player1, new SpitefulReturned());

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Bestow boosts the enchanted creature and triggers when it attacks")
    void bestowBoostsAndTriggersWhenEnchantedCreatureAttacks() {
        Permanent creature = addCreatureReady(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new SpitefulReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castWithAlternateCost(player1, 0, creature.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(3);

        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(15);
    }

    @Test
    void normalCastingNeedsNoTargetAndDoesNotBoostAnotherCreature() {
        Permanent creature = addCreatureReady(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new SpitefulReturned()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Spiteful Returned");
        assertThat(returned.isAttached()).isFalse();
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        returned.setSummoningSick(false);
        declareAttackers(player1, List.of(1));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void opponentEnchantedCreatureMakesItsDefendingPlayerLoseLife() {
        Permanent creature = addCreatureReady(player2, new FelhideBrawler());
        harness.setHand(player1, List.of(new SpitefulReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castWithAlternateCost(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player2, List.of(0));
            resolveAllTriggers();

            assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        });
    }

    @Test
    void bestowResolvesAsCreatureWhenTargetLeavesBeforeResolution() {
        Permanent creature = addCreatureReady(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new SpitefulReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castWithAlternateCost(player1, 0, creature.getId());
        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        Permanent returned = findPermanent(player1, "Spiteful Returned");
        assertThat(returned.isAttached()).isFalse();
        returned.setSummoningSick(false);
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void bestowBecomesCreatureWhenEnchantedCreatureLeaves() {
        Permanent creature = addCreatureReady(player1, new FelhideBrawler());
        harness.setHand(player1, List.of(new SpitefulReturned()));
        harness.addMana(player1, ManaColor.BLACK, 4);
        harness.castWithAlternateCost(player1, 0, creature.getId());
        harness.passBothPriorities();
        Permanent returned = findPermanent(player1, "Spiteful Returned");
        assertThat(returned.getAttachedTo()).isEqualTo(creature.getId());

        gd.playerBattlefields.get(player1.getId()).remove(creature);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(returned);
        assertThat(returned.isAttached()).isFalse();
        returned.setSummoningSick(false);
        declareAttackers(player1, List.of(0));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    void attackTriggerStillResolvesAfterSourceLeavesBattlefield() {
        Permanent returned = addCreatureReady(player1, new SpitefulReturned());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(player1, List.of(0));
            gd.playerBattlefields.get(player1.getId()).remove(returned);
            resolveAllTriggers();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        });
    }
}
