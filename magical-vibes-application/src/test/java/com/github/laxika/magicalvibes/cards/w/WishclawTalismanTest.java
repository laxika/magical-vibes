package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WishclawTalisman.class})
class WishclawTalismanTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three wish counters and tutors before giving control to an opponent")
    void tutorsThenGivesControlToOpponent() {
        harness.setLibrary(player1, List.of(new WishclawTalisman()));
        harness.setHand(player1, List.of(new WishclawTalisman()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        Permanent talisman = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThat(talisman.getCounterCount(CounterType.WISH)).isEqualTo(3);

        harness.activateAbility(player1, 0, null, null);

        assertThat(talisman.getCounterCount(CounterType.WISH)).isEqualTo(2);
        assertThat(talisman.isTapped()).isTrue();

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Wishclaw Talisman");

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Wishclaw Talisman");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(talisman);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(talisman);
    }

    @Test
    @DisplayName("Cannot activate outside its controller's turn")
    void cannotActivateOutsideControllersTurn() {
        harness.enterBattlefieldAndReturn(player1, new WishclawTalisman());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("during your turn");
    }

    @Test
    void emptyLibraryStillTransfersControlAndPreservesTappedState() {
        Permanent talisman = harness.enterBattlefieldAndReturn(player1, new WishclawTalisman());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(talisman);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(talisman);
        assertThat(talisman.isTapped()).isTrue();
        assertThat(talisman.getCounterCount(CounterType.WISH)).isEqualTo(2);
    }

    @Test
    void cannotActivateWithoutWishCounters() {
        Permanent talisman = harness.enterBattlefieldAndReturn(player1, new WishclawTalisman());
        talisman.setCounterCount(CounterType.WISH, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough counters");

        assertThat(talisman.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void newControllerSearchesTheirOwnLibraryAndReturnsControl() {
        Permanent talisman = harness.enterBattlefieldAndReturn(player1, new WishclawTalisman());
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player2);
        talisman.untap();
        harness.setLibrary(player2, List.of(new WishclawTalisman()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player2, 0);

        harness.assertInHand(player2, "Wishclaw Talisman");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(talisman);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(talisman);
        assertThat(talisman.getCounterCount(CounterType.WISH)).isEqualTo(1);
        assertThat(talisman.isTapped()).isTrue();
    }

    @Test
    void unrestrictedSearchCannotFailToFindFromNonemptyLibrary() {
        harness.enterBattlefieldAndReturn(player1, new WishclawTalisman());
        harness.setLibrary(player1, List.of(new WishclawTalisman()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleCardChosen(player1, -1))
                .isInstanceOf(IllegalStateException.class);

        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Wishclaw Talisman");
        assertThat(gd.playerBattlefields.get(player2.getId())).hasSize(1);
    }
}
