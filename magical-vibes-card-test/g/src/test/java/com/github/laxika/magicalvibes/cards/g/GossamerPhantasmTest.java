package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.o.Ovinize;
import com.github.laxika.magicalvibes.cards.r.RathiTrapper;
import com.github.laxika.magicalvibes.cards.s.Stingscourger;
import com.github.laxika.magicalvibes.cards.s.SuddenSpoiling;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GossamerPhantasm.class, Ovinize.class, RathiTrapper.class, Stingscourger.class, SuddenSpoiling.class})
class GossamerPhantasmTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of a spell")
    void sacrificesWhenTargetedBySpell() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new GossamerPhantasm());

        harness.setHand(player2, List.of(new Ovinize()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, phantasm.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertInGraveyard(player1, "Gossamer Phantasm");
    }

    @Test
    @DisplayName("Sacrifices itself when it becomes the target of an activated ability")
    void sacrificesWhenTargetedByAbility() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new GossamerPhantasm());
        Permanent rathiTrapper = addCreatureReady(player2, new RathiTrapper());

        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.activateAbility(player2, gd.playerBattlefields.get(player2.getId()).indexOf(rathiTrapper),
                null, phantasm.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertInGraveyard(player1, "Gossamer Phantasm");
    }

    @Test
    @DisplayName("Stays on the battlefield when it is not targeted")
    void staysWhenNotTargeted() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new GossamerPhantasm());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new RathiTrapper());

        harness.setHand(player2, List.of(new Ovinize()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, otherCreature.getId());

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(phantasm.getId()));
    }

    @Test
    @DisplayName("Its controller's spell also triggers sacrifice, before the spell resolves")
    void sacrificesBeforeItsControllersSpellResolves() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new GossamerPhantasm());
        harness.setHand(player1, List.of(new Ovinize()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, phantasm.getId());

        harness.assertOnBattlefield(player1, "Gossamer Phantasm");
        assertThat(gd.stack).hasSize(2);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertInGraveyard(player1, "Gossamer Phantasm");
        assertThat(gd.stack).hasSize(1);
        harness.assertNotInGraveyard(player1, "Ovinize");

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertInGraveyard(player1, "Ovinize");
    }

    @Test
    @DisplayName("Its controller's activated ability also triggers sacrifice")
    void sacrificesWhenTargetedByItsControllersAbility() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new GossamerPhantasm());
        Permanent trapper = addCreatureReady(player1, new RathiTrapper());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(trapper),
                null, phantasm.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertInGraveyard(player1, "Gossamer Phantasm");
        harness.assertOnBattlefield(player1, "Rathi Trapper");
        harness.passBothPriorities();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Sacrifices before a triggered ability can return it to hand")
    void sacrificesWhenTargetedByTriggeredAbility() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player2, new GossamerPhantasm());
        harness.setHand(player1, List.of(new Stingscourger()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, 0, phantasm.getId());
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Stingscourger");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Gossamer Phantasm");
        harness.assertInGraveyard(player2, "Gossamer Phantasm");
        harness.passBothPriorities();
        harness.assertNotInHand(player2, "Gossamer Phantasm");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Does not sacrifice when targeted after losing all abilities")
    void doesNotTriggerAfterLosingAllAbilities() {
        Permanent phantasm = harness.addToBattlefieldAndReturn(player1, new GossamerPhantasm());
        harness.setHand(player2, List.of(new SuddenSpoiling()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, player1.getId());
        harness.assertOnBattlefield(player1, "Gossamer Phantasm");

        harness.setHand(player2, List.of(new Ovinize()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, phantasm.getId());

        harness.assertOnBattlefield(player1, "Gossamer Phantasm");
        harness.assertNotInGraveyard(player1, "Gossamer Phantasm");
        harness.assertInGraveyard(player2, "Ovinize");
        assertThat(gd.stack).isEmpty();
    }
}
