package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.CoralFighters;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Soar;
import com.github.laxika.magicalvibes.cards.u.UnyaroGriffin;
import com.github.laxika.magicalvibes.cards.z.ZhalfirinKnight;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Floodgate.class, Soar.class, Island.class, ZhalfirinKnight.class, CoralFighters.class,
        UnyaroGriffin.class, FemerefScouts.class})
class FloodgateTest extends BaseCardTest {

    private Permanent addFloodgate() {
        return addCreatureReady(player1, new Floodgate());
    }

    private void addIslands(int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(player1, new Island());
        }
    }

    private Permanent grantFlying(Permanent target) {
        Permanent soar = harness.addToBattlefieldAndReturn(player1, new Soar());
        soar.setAttachedTo(target.getId());
        return soar;
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
        harness.setHand(player1, List.of(new Soar()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castEnchantment(player1, 0, floodgate.getId());
        harness.passBothPriorities(); // Soar resolves → state trigger goes on the stack
        harness.passBothPriorities(); // resolve it → sacrificed

        harness.assertNotOnBattlefield(player1, "Floodgate");
        harness.assertInGraveyard(player1, "Floodgate");
    }

    @Test
    @DisplayName("On leaving, deals half the Islands you control (rounded down) to nonblue creatures without flying")
    void dealsDamageOnLeavingBattlefield() {
        Permanent floodgate = addFloodgate();
        addIslands(5); // 5 / 2 = 2 damage
        harness.addToBattlefield(player1, new ZhalfirinKnight());
        harness.addToBattlefield(player2, new CoralFighters());
        harness.addToBattlefield(player2, new UnyaroGriffin());
        grantFlying(floodgate);

        harness.runStateBasedActions(); // state trigger
        harness.passBothPriorities(); // sacrifice → leaves-battlefield trigger
        harness.passBothPriorities(); // damage resolves

        harness.assertInGraveyard(player1, "Zhalfirin Knight");
        harness.assertOnBattlefield(player2, "Coral Fighters");
        harness.assertOnBattlefield(player2, "Unyaro Griffin");
    }

    @Test
    @DisplayName("Deals no damage with fewer than two Islands")
    void noDamageWithOneIsland() {
        Permanent floodgate = addFloodgate();
        addIslands(1); // 1 / 2 = 0 damage
        harness.addToBattlefield(player2, new FemerefScouts());
        grantFlying(floodgate);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Femeref Scouts");
    }

    @Test
    @DisplayName("Counts only Islands controlled by Floodgate's controller")
    void countsOnlyControllerIslands() {
        Permanent floodgate = addFloodgate();
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player2, new Island());
        harness.addToBattlefield(player1, new ZhalfirinKnight());
        grantFlying(floodgate);

        harness.runStateBasedActions();
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Zhalfirin Knight");
    }

    @Test
    @DisplayName("Evaluates the Island count when the leaves-the-battlefield trigger resolves")
    void evaluatesIslandCountAtResolution() {
        Permanent floodgate = addFloodgate();
        addIslands(2); // 2 / 2 = 1 damage when the trigger is created
        harness.addToBattlefield(player2, new ZhalfirinKnight());
        grantFlying(floodgate);

        harness.runStateBasedActions();
        addIslands(2); // The resolving trigger now sees 4 / 2 = 2 damage.
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Zhalfirin Knight");
    }

    @Test
    @DisplayName("Sacrifices even if flying is removed after the state trigger fires")
    void stateTriggerStillSacrificesAfterFlyingIsRemoved() {
        Permanent floodgate = addFloodgate();
        Permanent soar = grantFlying(floodgate);

        harness.runStateBasedActions();
        soar.setAttachedTo(null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Floodgate");
        harness.assertInGraveyard(player1, "Floodgate");
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
