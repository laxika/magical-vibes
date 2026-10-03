package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.n.NishobaBrawler;
import com.github.laxika.magicalvibes.cards.s.ShieldWallSentinel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CoralColony.class, NishobaBrawler.class, ShieldWallSentinel.class})
class CoralColonyTest extends BaseCardTest {

    @Test
    @DisplayName("Mills the target player for the number of defender creatures you control")
    void millsForControlledDefenderCount() {
        addCreatureReady(player1, new CoralColony());
        addCreatureReady(player1, new ShieldWallSentinel());
        addCreatureReady(player1, new NishobaBrawler());
        addCreatureReady(player2, new ShieldWallSentinel());

        List<Card> library = List.of(new NishobaBrawler(), new NishobaBrawler(), new NishobaBrawler());
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(library.get(2));
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(library.get(0), library.get(1));
    }

    @Test
    void canTargetItsControllerAndPaysManaAndTapCosts() {
        var colony = addCreatureReady(player1, new CoralColony());
        List<Card> library = List.of(new NishobaBrawler(), new NishobaBrawler());
        harness.setLibrary(player1, library);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, player1.getId());

        assertThat(colony.isTapped()).isTrue();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(1));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(library.get(0));
        colony.setTapped(false);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void countsDefendersAtResolutionIncludingNewDefenders() {
        addCreatureReady(player1, new CoralColony());
        List<Card> library = List.of(new NishobaBrawler(), new NishobaBrawler(), new NishobaBrawler());
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, 0, null, player2.getId());

        addCreatureReady(player1, new ShieldWallSentinel());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(library.get(2));
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(library.get(0), library.get(1));
    }

    @Test
    void resolvesAfterSourceLeavesAndCountsOnlyRemainingDefenders() {
        var colony = addCreatureReady(player1, new CoralColony());
        addCreatureReady(player1, new ShieldWallSentinel());
        List<Card> library = List.of(new NishobaBrawler(), new NishobaBrawler());
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, 0, null, player2.getId());

        gd.playerBattlefields.get(player1.getId()).remove(colony);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(library.get(1));
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(library.get(0));
    }

    @Test
    void millsZeroWhenNoDefendersRemainAtResolution() {
        var colony = addCreatureReady(player1, new CoralColony());
        List<Card> library = List.of(new NishobaBrawler());
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1, 0, 0, null, player2.getId());

        gd.playerBattlefields.get(player1.getId()).remove(colony);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(library);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    @Test
    void millsOnlyAvailableCardsWhenLibraryIsShorterThanDefenderCount() {
        addCreatureReady(player1, new CoralColony());
        addCreatureReady(player1, new ShieldWallSentinel());
        List<Card> library = List.of(new NishobaBrawler());
        harness.setLibrary(player2, library);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactlyElementsOf(library);
    }

    @Test
    void cannotActivateWhileSummoningSickOrWithoutBlueMana() {
        harness.addToBattlefield(player1, new CoralColony());
        harness.addMana(player1, ManaColor.BLUE, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);

        gd.playerBattlefields.get(player1.getId()).getFirst().setSummoningSick(false);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        gd.playerBattlefields.get(player1.getId()).getFirst().setTapped(false);
        harness.addMana(player1, ManaColor.WHITE, 2);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
