package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CourtHomunculus;
import com.github.laxika.magicalvibes.cards.r.ReliquaryTower;
import com.github.laxika.magicalvibes.cards.r.RuptureSpire;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkywardEyeProphets.class, ReliquaryTower.class, CourtHomunculus.class, RuptureSpire.class})
class SkywardEyeProphetsTest extends BaseCardTest {

    @Test
    @DisplayName("Revealed land card is put onto the battlefield")
    void landCardPutOntoBattlefield() {
        addCreatureReady(player1, new SkywardEyeProphets());
        Card land = new ReliquaryTower();
        gd.playerDecks.get(player1.getId()).addFirst(land);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getId().equals(land.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(land.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(land.getId()));
    }

    @Test
    @DisplayName("Revealed non-land card is put into the hand")
    void nonLandCardPutIntoHand() {
        addCreatureReady(player1, new SkywardEyeProphets());
        Card creature = new CourtHomunculus();
        gd.playerDecks.get(player1.getId()).addFirst(creature);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(creature.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getId().equals(creature.getId()));
    }

    @Test
    @DisplayName("Does nothing when the library is empty")
    void doesNothingWhenLibraryEmpty() {
        addCreatureReady(player1, new SkywardEyeProphets());
        harness.setLibrary(player1, List.of());
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Activation taps the Prophets and waits for resolution before revealing")
    void tapsAsCostAndRevealsOnResolution() {
        Permanent prophets = addCreatureReady(player1, new SkywardEyeProphets());
        Card topCard = new CourtHomunculus();
        harness.setLibrary(player1, List.of(topCard));
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);

        assertThat(prophets.isTapped()).isTrue();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(topCard);
        assertThat(gameLogContains("reveals Court Homunculus")).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new SkywardEyeProphets());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Skyward Eye Prophets").isTapped()).isFalse();
    }

    @Test
    @DisplayName("The ability uses the current top card even after its source leaves")
    void resolvesAfterSourceLeavesWithChangedLibrary() {
        Permanent prophets = addCreatureReady(player1, new SkywardEyeProphets());
        Card originalTop = new ReliquaryTower();
        Card newTop = new CourtHomunculus();
        harness.setLibrary(player1, List.of(originalTop));

        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(prophets);
        harness.setGraveyard(player1, List.of(prophets.getCard()));
        harness.setLibrary(player1, List.of(newTop, originalTop));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).contains(newTop);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalTop);
        harness.assertNotOnBattlefield(player1, "Reliquary Tower");
    }

    @Test
    @DisplayName("A revealed land retains its tapped entry and entry trigger")
    void revealedLandAppliesEntryAbilities() {
        addCreatureReady(player1, new SkywardEyeProphets());
        Card land = new RuptureSpire();
        harness.setLibrary(player1, List.of(land));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Rupture Spire").isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Rupture Spire");
        harness.assertInGraveyard(player1, "Rupture Spire");
    }
}
