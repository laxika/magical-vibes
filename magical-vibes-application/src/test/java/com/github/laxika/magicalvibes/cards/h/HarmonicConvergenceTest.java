package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ImpendingDisaster;
import com.github.laxika.magicalvibes.cards.r.Rivalry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HarmonicConvergenceTest extends BaseCardTest {

    @Test
    @DisplayName("Puts all enchantments on top of their owners' libraries")
    void putsAllEnchantmentsOnTopOfOwnersLibraries() {
        harness.addToBattlefield(player1, new Rivalry());
        harness.addToBattlefield(player2, new ImpendingDisaster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.setLibrary(player2, List.of(new Forest()));

        cast();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rivalry");
        harness.assertNotOnBattlefield(player2, "Impending Disaster");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Rivalry");
        assertThat(gd.playerDecks.get(player2.getId()).getFirst().getName()).isEqualTo("Impending Disaster");
        harness.assertInGraveyard(player1, "Harmonic Convergence");
    }

    @Test
    @DisplayName("Each owner chooses the order of multiple enchantments")
    void ownerChoosesOrderOfMultipleEnchantments() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new Rivalry());
        Permanent second = harness.addToBattlefieldAndReturn(player1, new ImpendingDisaster());
        harness.setLibrary(player1, List.of(new Forest()));

        cast();
        harness.passBothPriorities();

        PendingInteraction.MultiPermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiPermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(first.getId(), second.getId());

        harness.handleMultiplePermanentsChosen(player1, List.of(second.getId(), first.getId()));

        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Impending Disaster");
        assertThat(gd.playerDecks.get(player1.getId()).get(1).getName()).isEqualTo("Rivalry");
        harness.assertInGraveyard(player1, "Harmonic Convergence");
    }

    private void cast() {
        harness.setHand(player1, List.of(new HarmonicConvergence()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castInstant(player1, 0);
    }
}
