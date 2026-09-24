package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SkyhunterProwler;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GraftedWargear.class, SkyhunterProwler.class})
class GraftedWargearTest extends BaseCardTest {

    @Test
    void equippedCreatureGetsBoost() {
        Permanent creature = addCreatureReady(player1, new SkyhunterProwler());
        Permanent wargear = harness.addToBattlefieldAndReturn(player1, new GraftedWargear());
        wargear.setAttachedTo(creature.getId());

        assertThat(gqs.getEffectivePower(gd, creature)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(5);
    }

    @Test
    void equipZeroAttachesWithoutMana() {
        Permanent wargear = harness.addToBattlefieldAndReturn(player1, new GraftedWargear());
        Permanent creature = addCreatureReady(player1, new SkyhunterProwler());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(wargear.getAttachedTo()).isEqualTo(creature.getId());
    }

    @Test
    void reEquippingSacrificesPreviouslyEquippedCreature() {
        Permanent wargear = harness.addToBattlefieldAndReturn(player1, new GraftedWargear());
        Permanent creature1 = addCreatureReady(player1, new SkyhunterProwler());
        Permanent creature2 = addCreatureReady(player1, new SkyhunterProwler());
        wargear.setAttachedTo(creature1.getId());

        harness.activateAbility(player1, 0, null, creature2.getId());
        harness.passBothPriorities();

        assertThat(wargear.getAttachedTo()).isEqualTo(creature2.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(creature1.getId()));
        harness.assertInGraveyard(player1, "Skyhunter Prowler");
    }

    @Test
    void equippingFromUnattachedStateDoesNotSacrificeCreature() {
        Permanent wargear = harness.addToBattlefieldAndReturn(player1, new GraftedWargear());
        Permanent creature = addCreatureReady(player1, new SkyhunterProwler());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Skyhunter Prowler");
    }

    @Test
    void reEquippingToSameCreatureDoesNotSacrificeIt() {
        Permanent wargear = harness.addToBattlefieldAndReturn(player1, new GraftedWargear());
        Permanent creature = addCreatureReady(player1, new SkyhunterProwler());
        wargear.setAttachedTo(creature.getId());

        harness.activateAbility(player1, 0, null, creature.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(creature.getId()));
        harness.assertNotInGraveyard(player1, "Skyhunter Prowler");
    }

    @Test
    void removingEquipmentSacrificesEquippedCreature() {
        Permanent creature = addCreatureReady(player1, new SkyhunterProwler());
        Permanent wargear = harness.addToBattlefieldAndReturn(player1, new GraftedWargear());
        wargear.setAttachedTo(creature.getId());

        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, wargear));

        harness.assertInGraveyard(player1, "Grafted Wargear");
        harness.assertInGraveyard(player1, "Skyhunter Prowler");
    }
}
