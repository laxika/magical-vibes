package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WeatheredWayfarer.class, Forest.class, GlorySeeker.class, Plains.class})
class WeatheredWayfarerTest extends BaseCardTest {

    @Test
    @DisplayName("Cannot activate when no opponent controls more lands")
    void cannotActivateWithoutFewerLands() {
        addWayfarer();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addToBattlefield(player1, new Plains());
        harness.addToBattlefield(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent controls more lands");
    }

    @Test
    @DisplayName("Can activate when an opponent controls more lands — ability goes on the stack and taps")
    void activatesWhenOpponentHasMoreLands() {
        Permanent wayfarer = addWayfarer();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addToBattlefield(player2, new Forest());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(wayfarer.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Resolving presents only land cards for the search")
    void resolvingPresentsOnlyLands() {
        activateWithOpponentAhead();
        setupLibrary();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(c -> c.hasType(CardType.LAND));
    }

    @Test
    @DisplayName("Chosen land card goes to hand and shuffles library")
    void chosenLandGoesToHand() {
        activateWithOpponentAhead();
        setupLibrary();

        harness.passBothPriorities();

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.assertInHand(player1, "Plains");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Resolves without a prompt when the library has no land cards")
    void resolvesWithoutLandInLibrary() {
        activateWithOpponentAhead();
        harness.setLibrary(player1, List.of(new GlorySeeker()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertNotInHand(player1, "Glory Seeker");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    private Permanent addWayfarer() {
        return addCreatureReady(player1, new WeatheredWayfarer());
    }

    private void activateWithOpponentAhead() {
        addWayfarer();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addToBattlefield(player2, new Forest());
        harness.activateAbility(player1, 0, null, null);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new GlorySeeker()));
    }
}
