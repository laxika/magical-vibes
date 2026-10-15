package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FencerClique.class})
class FencerCliqueTest extends BaseCardTest {

    @Test
    @DisplayName("Activating {U} puts Fencer Clique on top of its owner's library")
    void activatePutsOnTopOfLibrary() {
        harness.addToBattlefield(player1, new FencerClique());

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);

        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fencer Clique");
        harness.assertNotInHand(player1, "Fencer Clique");
        assertThat(gd.playerDecks.get(player1.getId()).getFirst().getName()).isEqualTo("Fencer Clique");
    }

    @Test
    @DisplayName("Ability cannot be activated without paying {U}")
    void requiresMana() {
        harness.addToBattlefield(player1, new FencerClique());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Ability cannot be activated with non-blue mana")
    void requiresBlueMana() {
        harness.addToBattlefield(player1, new FencerClique());

        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, newly entered Fencer Clique can activate its ability")
    void tappedCreatureCanReturnToLibrary() {
        FencerClique clique = new FencerClique();
        var permanent = harness.addToBattlefieldAndReturn(player1, clique);
        permanent.tap();
        permanent.setSummoningSick(true);
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fencer Clique");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(clique);
    }

    @Test
    @DisplayName("A controlled Fencer Clique returns to its owner's library")
    void returnsToOwnersLibraryRatherThanControllers() {
        FencerClique clique = new FencerClique();
        clique.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, clique);
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Fencer Clique");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(clique);
    }

    @Test
    @DisplayName("Multiple activations do not move another Fencer Clique or duplicate the source")
    void secondActivationDoesNothingAfterSourceLeaves() {
        FencerClique source = new FencerClique();
        FencerClique other = new FencerClique();
        FencerClique libraryCard = new FencerClique();
        harness.addToBattlefield(player1, source);
        var otherPermanent = harness.addToBattlefieldAndReturn(player1, other);
        harness.setLibrary(player1, List.of(libraryCard));
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(otherPermanent);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(source, libraryCard);
    }
}
