package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.b.BullHippo;
import com.github.laxika.magicalvibes.cards.c.CoralMerfolk;
import com.github.laxika.magicalvibes.cards.e.EliteArchers;
import com.github.laxika.magicalvibes.cards.s.ShivanHellkite;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VenomousFangs.class, ShivanHellkite.class, BullHippo.class, CoralMerfolk.class,
        EliteArchers.class})
class VenomousFangsTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys another creature dealt noncombat damage by the enchanted creature")
    void destroysAnotherCreatureAfterNoncombatDamage() {
        Permanent source = addCreatureReady(player1, new ShivanHellkite());
        Permanent target = addCreatureReady(player2, new BullHippo());
        attachFangs(player2, source);
        addShivanHellkiteMana();

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Bull Hippo");
        harness.assertOnBattlefield(player1, "Shivan Hellkite");
    }

    @Test
    @DisplayName("Does not trigger when the enchanted creature deals damage to a player")
    void doesNotTriggerOnDamageToPlayer() {
        Permanent source = addCreatureReady(player1, new ShivanHellkite());
        addCreatureReady(player2, new BullHippo());
        attachFangs(player2, source);
        harness.setLife(player2, 20);
        addShivanHellkiteMana();

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
        harness.assertOnBattlefield(player2, "Bull Hippo");
    }

    @Test
    @DisplayName("Does not destroy the enchanted creature when it damages itself")
    void doesNotDestroyEnchantedCreatureWhenItDamagesItself() {
        Permanent source = addCreatureReady(player1, new ShivanHellkite());
        attachFangs(player2, source);
        addShivanHellkiteMana();

        harness.activateAbility(player1, 0, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Shivan Hellkite");
    }

    @Test
    @DisplayName("Queues the trigger before lethal damage removes the damaged creature")
    void queuesTriggerBeforeLethalDamageStateBasedAction() {
        Permanent source = addCreatureReady(player1, new EliteArchers());
        Permanent target = addCreatureReady(player2, new BullHippo());
        attachFangs(player2, source);
        target.setAttacking(true);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.assertInGraveyard(player2, "Bull Hippo");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Combat trigger is collected before the enchanted creature and Aura die")
    void combatTriggerSurvivesSourceAndAuraLeaving() {
        Permanent source = addCreatureReady(player1, new CoralMerfolk());
        Permanent target = addCreatureReady(player2, new BullHippo());
        attachFangs(player2, source);
        source.setAttacking(true);
        target.setBlocking(true);
        target.addBlockingTarget(0);

        resolveCombat();

        harness.assertNotOnBattlefield(player2, "Bull Hippo");
        harness.assertInGraveyard(player2, "Venomous Fangs");
        harness.assertInGraveyard(player1, "Coral Merfolk");
    }

    private void addShivanHellkiteMana() {
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    private void attachFangs(Player controller, Permanent creature) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new VenomousFangs());
        aura.setAttachedTo(creature.getId());
    }
}
