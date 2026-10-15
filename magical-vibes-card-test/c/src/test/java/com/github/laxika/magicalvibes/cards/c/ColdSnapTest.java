package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.i.IvoryMask;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.SnowCoveredPlains;
import com.github.laxika.magicalvibes.cards.z.ZuranOrb;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ColdSnap.class, Plains.class, SnowCoveredPlains.class, Disenchant.class, IvoryMask.class, ZuranOrb.class})
class ColdSnapTest extends BaseCardTest {

    private void addSnowLands(Player controller, int count) {
        for (int i = 0; i < count; i++) {
            harness.addToBattlefield(controller, new SnowCoveredPlains());
        }
    }

    @Test
    @DisplayName("Deals damage to the active player equal to snow lands they control")
    void damagesActivePlayerBySnowLandCount() {
        harness.addToBattlefield(player1, new ColdSnap());
        addSnowLands(player2, 3);

        // Opponent's upkeep: only the damage trigger fires (CU is controller-only)
        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Damages each player based on their own snow lands during their own upkeep")
    void damagesEachPlayerByOwnSnowLands() {
        harness.addToBattlefield(player1, new ColdSnap());
        addSnowLands(player2, 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Deals no damage when the active player controls no snow lands")
    void noDamageWithoutSnowLands() {
        harness.addToBattlefield(player1, new ColdSnap());
        // Non-snow land does not count
        harness.addToBattlefield(player2, new Plains());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Counts only snow lands controlled by the active player")
    void countsActivePlayersSnowLandsOnly() {
        harness.addToBattlefield(player1, new ColdSnap());
        addSnowLands(player1, 1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps Cold Snap")
    void paysCumulativeUpkeep() {
        Permanent snap = harness.addToBattlefieldAndReturn(player1, new ColdSnap());

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(snap.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(snap);
    }

    @Test
    @DisplayName("Declining cumulative upkeep sacrifices Cold Snap")
    void declineSacrifices() {
        Permanent snap = harness.addToBattlefieldAndReturn(player1, new ColdSnap());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(snap);
        harness.assertInGraveyard(player1, "Cold Snap");
    }

    @Test
    @DisplayName("Second cumulative upkeep costs four mana")
    void secondCumulativeUpkeepCostsFourMana() {
        Permanent snap = harness.addToBattlefieldAndReturn(player1, new ColdSnap());

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(snap.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(snap);
    }

    @Test
    @DisplayName("Damages the controller during their own upkeep")
    void damagesControllerDuringOwnUpkeep() {
        harness.addToBattlefield(player1, new ColdSnap());
        addSnowLands(player1, 2);
        addSnowLands(player2, 3);

        advanceToUpkeep(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
        harness.assertInGraveyard(player1, "Cold Snap");
    }

    @Test
    @DisplayName("Shroud does not stop the nontargeted upkeep damage")
    void damagesPlayerWithShroud() {
        harness.addToBattlefield(player1, new ColdSnap());
        harness.addToBattlefield(player2, new IvoryMask());
        addSnowLands(player2, 3);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Upkeep damage resolves even if Cold Snap is destroyed in response")
    void damageResolvesAfterSourceIsDestroyed() {
        Permanent snap = harness.addToBattlefieldAndReturn(player1, new ColdSnap());
        addSnowLands(player2, 2);
        harness.setHand(player2, List.of(new Disenchant()));

        advanceToUpkeep(player2);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, snap.getId());
        harness.assertInGraveyard(player1, "Cold Snap");
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Snow lands are counted when the upkeep ability resolves")
    void countsSnowLandsAtResolution() {
        harness.addToBattlefield(player1, new ColdSnap());
        harness.addToBattlefield(player2, new ZuranOrb());
        addSnowLands(player2, 3);
        Permanent land = findPermanent(player2, "Snow-Covered Plains");

        advanceToUpkeep(player2);
        harness.activateAbility(player2, 0, 0, null, null);
        harness.handlePermanentChosen(player2, land.getId());
        harness.passBothPriorities();
        harness.assertLife(player2, 22);
        harness.assertInGraveyard(player2, "Snow-Covered Plains");
        resolveAllTriggers();

        harness.assertLife(player2, 20);
    }
}
