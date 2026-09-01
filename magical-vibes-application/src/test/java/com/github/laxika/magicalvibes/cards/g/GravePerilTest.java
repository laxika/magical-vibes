package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({GravePeril.class, GrizzlyBears.class, ScatheZombies.class})
class GravePerilTest extends BaseCardTest {

    @Test
    @DisplayName("A nonblack creature entering sacrifices Grave Peril and is destroyed")
    void nonblackCreatureEntrySacrificesAndDestroys() {
        harness.addToBattlefield(player1, new GravePeril());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grave Peril");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("A black creature entering does not trigger Grave Peril")
    void blackCreatureEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new GravePeril());
        harness.setHand(player1, List.of(new ScatheZombies()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grave Peril");
        harness.assertOnBattlefield(player1, "Scathe Zombies");
    }

    @Test
    @DisplayName("If Grave Peril leaves before its trigger resolves, the entering creature survives")
    void sourceLeavingBeforeResolutionPreventsDestruction() {
        harness.addToBattlefield(player1, new GravePeril());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        var gravePeril = findPermanent(player1, "Grave Peril");
        gd.playerBattlefields.get(player1.getId()).remove(gravePeril);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }
}
