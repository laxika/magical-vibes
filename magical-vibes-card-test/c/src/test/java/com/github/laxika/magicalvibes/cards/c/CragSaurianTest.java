package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Dominate;
import com.github.laxika.magicalvibes.cards.k.KrisMage;
import com.github.laxika.magicalvibes.cards.s.ShockTroops;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CragSaurian.class, CaveIn.class, ShockTroops.class, CateranBrute.class, KrisMage.class, Dominate.class})
class CragSaurianTest extends BaseCardTest {

    @Test
    @DisplayName("spell damage makes its controller gain control after the trigger resolves")
    void spellDamageChangesControl() {
        harness.addToBattlefield(player2, new CragSaurian());
        harness.setHand(player1, List.of(new CaveIn()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crag Saurian");
        harness.assertNotOnBattlefield(player2, "Crag Saurian");
    }

    @Test
    @DisplayName("damage from a creature ability makes its controller gain control after the trigger resolves")
    void creatureAbilityDamageChangesControl() {
        harness.addToBattlefield(player2, new CragSaurian());
        addCreatureReady(player1, new ShockTroops());

        UUID cragSaurianId = harness.getPermanentId(player2, "Crag Saurian");
        harness.activateAbility(player1, 0, null, cragSaurianId);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Crag Saurian");
        harness.assertInGraveyard(player1, "Shock Troops");
        harness.assertNotOnBattlefield(player2, "Crag Saurian");
    }

    @Test
    @DisplayName("combat damage makes the source's controller gain control after the trigger resolves")
    void combatDamageChangesControl() {
        addCreatureReady(player1, new CateranBrute());
        addCreatureReady(player2, new CragSaurian());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat(player1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Crag Saurian");
        harness.assertNotOnBattlefield(player2, "Crag Saurian");
    }

    @Test
    @DisplayName("the damage source's controller is determined when the trigger resolves")
    void usesDamageSourcesCurrentController() {
        addCreatureReady(player1, new KrisMage());
        harness.addToBattlefield(player2, new CragSaurian());
        harness.setHand(player1, List.of(new CaveIn()));
        harness.setHand(player2, List.of(new Dominate()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        UUID mageId = harness.getPermanentId(player1, "Kris Mage");
        UUID saurianId = harness.getPermanentId(player2, "Crag Saurian");

        harness.activateAbility(player1, 0, 0, null, saurianId);
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.castInstant(player2, 0, 1, mageId);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Kris Mage");
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Crag Saurian");
        harness.assertNotOnBattlefield(player1, "Crag Saurian");
    }

    @Test
    @DisplayName("lethal damage does not move Crag Saurian out of its owner's graveyard")
    void lethalDamageDoesNotGainControl() {
        harness.addToBattlefield(player2, new CragSaurian());
        addCreatureReady(player1, new ShockTroops());
        addCreatureReady(player1, new ShockTroops());
        UUID saurianId = harness.getPermanentId(player2, "Crag Saurian");

        harness.activateAbility(player1, 0, null, saurianId);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.activateAbility(player1, 0, null, saurianId);
        harness.passBothPriorities();
        harness.assertInGraveyard(player2, "Crag Saurian");
        resolveAllTriggers();

        harness.assertInGraveyard(player2, "Crag Saurian");
        harness.assertNotOnBattlefield(player1, "Crag Saurian");
        harness.assertNotOnBattlefield(player2, "Crag Saurian");
    }
}

