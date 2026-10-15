package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.Boomerang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MycosynthLattice;
import com.github.laxika.magicalvibes.cards.s.Shatter;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WeaponsManufacturing.class, Spellbook.class, GrizzlyBears.class, Shatter.class, Boomerang.class,
        MycosynthLattice.class})
class WeaponsManufacturingTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Munitions token when a nontoken artifact enters under your control")
    void createsMunitionsForNontokenArtifact() {
        harness.addToBattlefield(player1, new WeaponsManufacturing());
        harness.setHand(player1, List.of(new Spellbook()));

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Munitions")).hasSize(1);
    }

    @Test
    @DisplayName("Does not create Munitions when a creature enters")
    void doesNotTriggerForCreature() {
        harness.addToBattlefield(player1, new WeaponsManufacturing());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Munitions")).isEmpty();
    }

    @Test
    @DisplayName("Munitions deals 2 damage to a chosen target when it leaves")
    void munitionsDealsDamageWhenLeaving() {
        harness.addToBattlefield(player1, new WeaponsManufacturing());
        harness.setHand(player1, List.of(new Spellbook()));
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent munitions = findPermanents(player1, "Munitions").getFirst();
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.setLife(player2, 20);

        harness.castAndResolveInstant(player1, 0, munitions.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertNotOnBattlefield(player1, "Munitions");
    }

    @Test
    @DisplayName("Munitions entering does not trigger another Munitions token")
    void doesNotTriggerForMunitionsToken() {
        harness.addToBattlefield(player1, new WeaponsManufacturing());
        harness.enterBattlefieldAndReturn(player1, new Spellbook());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Munitions")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An opponent's nontoken artifact does not create Munitions")
    void doesNotTriggerForOpponentsArtifact() {
        harness.addToBattlefield(player1, new WeaponsManufacturing());
        harness.enterBattlefieldAndReturn(player2, new Spellbook());

        assertThat(findPermanents(player1, "Munitions")).isEmpty();
        assertThat(findPermanents(player2, "Munitions")).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each nontoken artifact entering creates a separate Munitions token")
    void createsMunitionsForEachArtifact() {
        harness.addToBattlefield(player1, new WeaponsManufacturing());
        harness.enterBattlefieldAndReturn(player1, new Spellbook());
        harness.passBothPriorities();
        harness.enterBattlefieldAndReturn(player1, new Spellbook());
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Munitions")).hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Returning Munitions to hand triggers damage even after Weapons Manufacturing leaves")
    void munitionsTriggersOnBounceWithoutEnchantment() {
        harness.addToBattlefield(player1, new WeaponsManufacturing());
        harness.enterBattlefieldAndReturn(player1, new Spellbook());
        harness.passBothPriorities();
        Permanent munitions = findPermanent(player1, "Munitions");
        harness.setHand(player1, List.of(new Boomerang(), new Boomerang()));
        harness.addMana(player1, ManaColor.BLUE, 4);
        harness.setLife(player1, 20);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Weapons Manufacturing"));
        harness.castAndResolveInstant(player1, 0, munitions.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 18);
        harness.assertNotOnBattlefield(player1, "Munitions");
        harness.assertNotInHand(player1, "Munitions");
    }

    @Test
    @DisplayName("Munitions can deal lethal damage to a creature")
    void munitionsCanTargetCreature() {
        harness.addToBattlefield(player1, new WeaponsManufacturing());
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.enterBattlefieldAndReturn(player1, new Spellbook());
        harness.passBothPriorities();
        Permanent munitions = findPermanent(player1, "Munitions");
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castAndResolveInstant(player1, 0, munitions.getId());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, creature.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Weapons Manufacturing creates Munitions for its own entry when it enters as an artifact")
    void triggersForItsOwnArtifactEntry() {
        harness.addToBattlefield(player1, new MycosynthLattice());
        harness.setHand(player1, List.of(new WeaponsManufacturing()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Munitions")).hasSize(1);
    }
}
