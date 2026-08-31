package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FamishedWorldsire.class, GrizzlyBears.class, Mountain.class})
class FamishedWorldsireTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed lands add counters and power determines the revealed cards")
    void sacrificedLandsAddCountersAndPowerDeterminesRevealCount() {
        Permanent sacrificedMountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        Permanent sacrificedMountainTwo = harness.addToBattlefieldAndReturn(player1, new Mountain());
        List<Card> library = new ArrayList<>(List.of(
                new Mountain(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new Mountain(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setLibrary(player1, library);

        castWorldsire();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiPermanentChoice.class);
        harness.handleMultiplePermanentsChosen(player1,
                List.of(sacrificedMountain.getId(), sacrificedMountainTwo.getId()));

        Permanent worldsire = findPermanent(player1, "Famished Worldsire");
        assertThat(worldsire.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);

        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).hasSize(6);
        assertThat(choice.validCardIds()).containsExactly(
                library.get(0).getId(), library.get(4).getId());
        assertThat(choice.selectedToBattlefieldTapped()).isTrue();

        harness.handleMultipleCardsChosen(player1, choice.validCardIds());

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(
                library.get(1), library.get(2), library.get(3), library.get(5), library.get(6));
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().equals(library.get(0))
                        || permanent.getCard().equals(library.get(4)))
                .allMatch(Permanent::isTapped)).isTrue();
    }

    @Test
    @DisplayName("Choosing no lands leaves the lands and makes the 0/0 die")
    void choosingNoLandsLeavesTheLandsAndWorldsireDies() {
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());

        castWorldsire();
        harness.passBothPriorities();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(countPermanents(player1, "Famished Worldsire")).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(
                mountain);
    }

    private void castWorldsire() {
        harness.setHand(player1, new ArrayList<>(List.of(new FamishedWorldsire())));
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0);
    }
}
