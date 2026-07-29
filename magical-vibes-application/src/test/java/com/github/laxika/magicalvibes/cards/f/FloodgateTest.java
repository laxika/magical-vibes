package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FloodgateTest extends BaseCardTest {

    private Permanent addFloodgate() {
        Permanent floodgate = new Permanent(new Floodgate());
        floodgate.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(floodgate);
        return floodgate;
    }

    private void addIslands(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Island());
        }
    }

    private void grantFlying(Permanent target) {
        Permanent flight = new Permanent(new Flight());
        flight.setAttachedTo(target.getId());
        gd.playerBattlefields.get(player1.getId()).add(flight);
    }

    @Test
    @DisplayName("Survives on the battlefield while it doesn't have flying")
    void survivesWithoutFlying() {
        addFloodgate();

        harness.runStateBasedActions();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Floodgate");
    }

    @Test
    @DisplayName("Sacrificed when it gains flying")
    void sacrificedWhenItHasFlying() {
        Permanent floodgate = addFloodgate();
        harness.setHand(player1, List.of(new Flight()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castEnchantment(player1, 0, floodgate.getId());
        harness.passBothPriorities(); // Flight resolves → state trigger goes on the stack
        harness.passBothPriorities(); // resolve it → sacrificed

        harness.assertNotOnBattlefield(player1, "Floodgate");
        harness.assertInGraveyard(player1, "Floodgate");
    }

    @Test
    @DisplayName("On leaving, deals half the Islands you control (rounded down) to nonblue creatures without flying")
    void dealsDamageOnLeavingBattlefield() {
        Permanent floodgate = addFloodgate();
        addIslands(5); // 5 / 2 = 2 damage
        harness.addToBattlefield(player1, new GrizzlyBears());   // green 2/2 — dies
        harness.addToBattlefield(player2, new FugitiveWizard()); // blue 1/1 — untouched
        harness.addToBattlefield(player2, new SuntailHawk());    // white 1/1 flier — untouched
        grantFlying(floodgate);

        harness.runStateBasedActions(); // state trigger
        harness.passBothPriorities(); // sacrifice → leaves-battlefield trigger
        harness.passBothPriorities(); // damage resolves

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Fugitive Wizard");
        harness.assertOnBattlefield(player2, "Suntail Hawk");
    }

    @Test
    @DisplayName("Deals no damage with fewer than two Islands")
    void noDamageWithOneIsland() {
        Permanent floodgate = addFloodgate();
        addIslands(1); // 1 / 2 = 0 damage
        harness.addToBattlefield(player2, new LlanowarElves()); // green 1/1 — survives
        grantFlying(floodgate);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Llanowar Elves");
    }

    @Test
    @DisplayName("Cast from hand it stays on the battlefield")
    void castStaysOnBattlefield() {
        harness.setHand(player1, List.of(new Floodgate()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Floodgate");
    }
}
