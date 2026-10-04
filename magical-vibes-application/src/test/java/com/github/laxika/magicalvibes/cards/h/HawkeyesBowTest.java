package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LoxodonWarhammer;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({HawkeyesBow.class, GrizzlyBears.class, LoxodonWarhammer.class})
class HawkeyesBowTest extends BaseCardTest {

    @Test
    @DisplayName("Equipped creature gets +1/+0 and reach")
    void equippedCreatureGetsBoostAndReach() {
        Permanent bow = addReadyBow(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        bow.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Equip attaches Hawkeye's Bow to a creature")
    void equipsCreature() {
        Permanent bow = addReadyBow(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(bow.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    @DisplayName("Equipped creature becoming tapped deals 1 damage to each opponent")
    void equippedCreatureBecomingTappedDamagesOpponents() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent bow = addReadyBow(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        bow.setAttachedTo(creature.getId());

        creature.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, creature));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("The ability also triggers when an opponent controls the equipped creature")
    void opponentControlledEquippedCreatureTriggers() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent bow = addReadyBow(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        bow.setAttachedTo(creature.getId());

        creature.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, creature));
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("An unattached creature does not trigger Hawkeye's Bow")
    void unattachedCreatureDoesNotTrigger() {
        addReadyBow(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());

        creature.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, creature));

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The equipped creature's lifelink applies to the tap damage")
    void equippedCreatureLifelinkApplies() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent bow = addReadyBow(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new LoxodonWarhammer());
        bow.setAttachedTo(creature.getId());
        hammer.setAttachedTo(creature.getId());

        creature.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, creature));
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Lifelink benefits the creature's controller while opponents are determined by the Bow's controller")
    void opponentControlledCreatureGainsLifeFromItsDamage() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent bow = addReadyBow(player1);
        Permanent creature = addCreatureReady(player2, new GrizzlyBears());
        Permanent hammer = harness.addToBattlefieldAndReturn(player2, new LoxodonWarhammer());
        bow.setAttachedTo(creature.getId());
        hammer.setAttachedTo(creature.getId());

        creature.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, creature));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Moving the Bow after it triggers does not change the creature that deals damage")
    void movingBowDoesNotChangePendingDamageSource() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        Permanent bow = addReadyBow(player1);
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent hammer = harness.addToBattlefieldAndReturn(player1, new LoxodonWarhammer());
        bow.setAttachedTo(creature.getId());
        hammer.setAttachedTo(creature.getId());

        creature.tap();
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .checkEnchantedPermanentTapTriggers(gd, creature));
        bow.setAttachedTo(otherCreature.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.REACH)).isFalse();
        assertThat(gqs.hasKeyword(gd, otherCreature, Keyword.REACH)).isTrue();
    }

    private Permanent addReadyBow(Player player) {
        Permanent bow = harness.addToBattlefieldAndReturn(player, new HawkeyesBow());
        bow.setSummoningSick(false);
        return bow;
    }
}
