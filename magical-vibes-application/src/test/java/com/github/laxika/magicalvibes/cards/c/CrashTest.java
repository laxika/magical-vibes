package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BargainingTable;
import com.github.laxika.magicalvibes.cards.f.FreshVolunteers;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Crash.class, BargainingTable.class, FreshVolunteers.class, Island.class, Mountain.class})
class CrashTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys target artifact when cast for mana")
    void destroysArtifactForManaCost() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BargainingTable());
        harness.setHand(player1, List.of(new Crash()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertNotOnBattlefield(player2, "Bargaining Table");
        harness.assertInGraveyard(player2, "Bargaining Table");
    }

    @Test
    @DisplayName("Alternate cost sacrifices a Mountain")
    void castBySacrificingMountain() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new BargainingTable());
        harness.setHand(player1, List.of(new Crash()));

        harness.castInstantWithAlternateCost(player1, 0, target.getId(), List.of(mountain.getId()));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mountain");
        harness.assertInGraveyard(player1, "Mountain");
        harness.assertNotOnBattlefield(player2, "Bargaining Table");
        harness.assertInGraveyard(player2, "Bargaining Table");
    }

    @Test
    @DisplayName("Alternate cost fails when sacrificing a non-Mountain")
    void alternateCostFailsWithNonMountain() {
        harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent island = harness.addToBattlefieldAndReturn(player1, new Island());
        harness.setHand(player1, List.of(new Crash()));

        assertThatThrownBy(() ->
                harness.castInstantWithAlternateCost(player1, 0, null, List.of(island.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature")
    void cannotTargetCreature() {
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new FreshVolunteers());
        harness.setHand(player1, List.of(new Crash()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
