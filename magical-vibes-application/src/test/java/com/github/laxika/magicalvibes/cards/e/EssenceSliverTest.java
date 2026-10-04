package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.FireWhip;
import com.github.laxika.magicalvibes.cards.m.MoorishCavalry;
import com.github.laxika.magicalvibes.cards.s.SpinedSliver;
import com.github.laxika.magicalvibes.cards.s.Squire;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EssenceSliver.class, FireWhip.class, MoorishCavalry.class, SpinedSliver.class, Squire.class})
class EssenceSliverTest extends BaseCardTest {

    @Test
    @DisplayName("Essence Sliver gains life for the damage it deals")
    void gainsLifeForItsOwnDamage() {
        addCreatureReady(player1, new EssenceSliver());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 23);
    }

    @Test
    @DisplayName("An opposing Sliver also gains life for the damage it deals")
    void opposingSliverGainsLifeForItsDamage() {
        addCreatureReady(player1, new EssenceSliver());
        addCreatureReady(player2, new SpinedSliver());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(player2, List.of(0));
        gs.declareBlockers(gd, player1, List.of());
        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 22);
    }

    @Test
    @DisplayName("Essence Sliver does not grant the ability to non-Slivers")
    void doesNotGrantAbilityToNonSlivers() {
        addCreatureReady(player1, new EssenceSliver());
        addCreatureReady(player1, new Squire());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(1));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A Sliver gains life when it deals combat damage to a creature")
    void gainsLifeForCombatDamageToCreature() {
        addCreatureReady(player1, new EssenceSliver());
        addCreatureReady(player2, new Squire());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player2, "Squire");
    }

    @Test
    @DisplayName("Essence Sliver still triggers after dealing lethal combat damage")
    void sourceStillTriggersAfterDying() {
        addCreatureReady(player1, new EssenceSliver());
        addCreatureReady(player2, new MoorishCavalry());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 23);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Essence Sliver");
        harness.assertInGraveyard(player2, "Moorish Cavalry");
    }

    @Test
    @DisplayName("Essence Sliver gains life for noncombat damage")
    void gainsLifeForNoncombatDamage() {
        Permanent essence = addCreatureReady(player1, new EssenceSliver());
        Permanent fireWhip = harness.addToBattlefieldAndReturn(player1, new FireWhip());
        fireWhip.setAttachedTo(essence.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Essence Sliver controls and is the source of the trigger for an opposing Sliver")
    void essenceControlsTriggerForOpposingSliverDamage() {
        Permanent essence = addCreatureReady(player1, new EssenceSliver());
        Permanent spined = addCreatureReady(player2, new SpinedSliver());
        Permanent fireWhip = harness.addToBattlefieldAndReturn(player2, new FireWhip());
        fireWhip.setAttachedTo(spined.getId());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getControllerId()).isEqualTo(player1.getId());
        assertThat(gd.stack.getFirst().getSourcePermanentId()).isEqualTo(essence.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 21);
    }

    @Test
    @DisplayName("Multiple Essence Slivers each trigger for the same damage")
    void multipleEssenceSliversGainLifeSeparately() {
        addCreatureReady(player1, new EssenceSliver());
        addCreatureReady(player1, new EssenceSliver());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        declareAttackers(player1, List.of(0));
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player1, 26);
        harness.assertLife(player2, 17);
    }
}
