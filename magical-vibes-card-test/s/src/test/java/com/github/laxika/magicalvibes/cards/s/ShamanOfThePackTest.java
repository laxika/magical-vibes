package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TimberpackWolf;
import com.github.laxika.magicalvibes.cards.l.LeafGilder;
import com.github.laxika.magicalvibes.cards.u.UnholyHunger;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ShamanOfThePack.class, LeafGilder.class, TimberpackWolf.class, UnholyHunger.class})
class ShamanOfThePackTest extends BaseCardTest {

    private void castShaman() {
        harness.setHand(player1, List.of(new ShamanOfThePack()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Target opponent loses life equal to the Elves you control, counting the Shaman itself")
    void drainsForEachElfIncludingItself() {
        harness.addToBattlefield(player1, new LeafGilder());
        harness.addToBattlefield(player1, new LeafGilder());
        harness.addToBattlefield(player1, new TimberpackWolf());
        harness.addToBattlefield(player2, new LeafGilder());

        castShaman();
        harness.passBothPriorities(); // resolve the ETB trigger

        // Two Leaf Gilders + the Shaman itself; the Wolf and the opponent's Elf don't count.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("With no other Elves the opponent still loses 1 life for the Shaman itself")
    void drainsOneWithNoOtherElves() {
        castShaman();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Elves entering after the trigger is stacked count at resolution")
    void countsElvesAddedBeforeResolution() {
        castShaman();
        harness.addToBattlefield(player1, new LeafGilder());

        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("An Elf destroyed in response no longer counts")
    void excludesElfRemovedBeforeResolution() {
        var elf = harness.addToBattlefieldAndReturn(player1, new LeafGilder());
        castShaman();

        destroyInResponse(elf.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("The trigger resolves for zero if the only Elf was the Shaman and it is destroyed")
    void resolvesForZeroAfterSourceLeaves() {
        castShaman();

        destroyInResponse(harness.getPermanentId(player1, "Shaman of the Pack"));
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);
        harness.assertInGraveyard(player1, "Shaman of the Pack");
    }

    @Test
    @DisplayName("The trigger still counts remaining Elves when the Shaman leaves")
    void resolvesAfterSourceLeavesWithOtherElves() {
        harness.addToBattlefield(player1, new LeafGilder());
        castShaman();

        destroyInResponse(harness.getPermanentId(player1, "Shaman of the Pack"));
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);
    }

    private void destroyInResponse(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new UnholyHunger()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, targetId);
    }
}
