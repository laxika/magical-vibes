package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GiantKoi.class, Island.class})
class GiantKoiTest extends BaseCardTest {

    @Test
    @DisplayName("Waterbend makes Giant Koi unblockable this turn")
    void waterbendMakesGiantKoiUnblockable() {
        Permanent koi = harness.addToBattlefieldAndReturn(player1, new GiantKoi());
        Permanent firstCreature = harness.addToBattlefieldAndReturn(player1, new GiantKoi());
        Permanent secondCreature = harness.addToBattlefieldAndReturn(player1, new GiantKoi());

        harness.activateAbility(player1, 0, null, null);

        assertThat(koi.isTapped()).isTrue();
        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(koi.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("The Waterbend unblockable effect wears off at end of turn")
    void waterbendUnblockableWearsOffAtEndOfTurn() {
        Permanent koi = harness.addToBattlefieldAndReturn(player1, new GiantKoi());
        harness.addToBattlefieldAndReturn(player1, new GiantKoi());
        harness.addToBattlefieldAndReturn(player1, new GiantKoi());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(koi.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(koi.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Waterbend cannot be paid without three available payments")
    void waterbendRequiresThreePayments() {
        Permanent koi = harness.addToBattlefieldAndReturn(player1, new GiantKoi());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GiantKoi());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("waterbend");

        assertThat(koi.isTapped()).isFalse();
        assertThat(creature.isTapped()).isFalse();
        assertThat(koi.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Islandcycling searches for an Island and discards Giant Koi")
    void islandcyclingSearchesForIsland() {
        Card koi = new GiantKoi();
        Island island = new Island();
        harness.setHand(player1, List.of(koi));
        harness.setLibrary(player1, List.of(island, new GiantKoi()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(koi);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .hasSize(1)
                .allMatch(card -> card instanceof Island);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(island);
    }
    @Test
    @DisplayName("Waterbend can be paid entirely with mana without tapping Giant Koi")
    void waterbendWithMana() {
        Permanent koi = harness.addToBattlefieldAndReturn(player1, new GiantKoi());
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        assertThat(koi.isTapped()).isFalse();
        assertThat(koi.isCantBeBlocked()).isFalse();
        harness.passBothPriorities();

        assertThat(koi.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Waterbend can combine mana and tapping a summoning-sick creature")
    void waterbendWithMixedPayment() {
        Permanent koi = harness.addToBattlefieldAndReturn(player1, new GiantKoi());
        koi.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        assertThat(koi.isTapped()).isTrue();
        harness.passBothPriorities();

        assertThat(koi.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Islandcycling may fail to find an Island even when one is available")
    void islandcyclingMayFailToFind() {
        Card koi = new GiantKoi();
        Island island = new Island();
        harness.setHand(player1, List.of(koi));
        harness.setLibrary(player1, List.of(island));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(koi);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(island);
    }

    @Test
    @DisplayName("Islandcycling without an Island still discards Giant Koi")
    void islandcyclingWithoutIsland() {
        Card koi = new GiantKoi();
        Card other = new GiantKoi();
        harness.setHand(player1, List.of(koi));
        harness.setLibrary(player1, List.of(other));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).contains(koi);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
