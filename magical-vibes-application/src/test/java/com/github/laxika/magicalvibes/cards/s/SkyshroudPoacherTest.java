package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.m.Mossdog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkyshroudPoacher.class, SkyshroudSentinel.class, Mossdog.class})
class SkyshroudPoacherTest extends BaseCardTest {

    private void setUpPoacher() {
        addCreatureReady(player1, new SkyshroudPoacher());
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }

    @Test
    @DisplayName("The ability offers only Elf permanent cards")
    void searchOffersOnlyElves() {
        setUpPoacher();
        harness.setLibrary(player1, List.of(new SkyshroudSentinel(), new Mossdog()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .extracting(Card::getName)
                .containsExactly("Skyshroud Sentinel");
    }

    @Test
    @DisplayName("The found Elf enters the battlefield")
    void foundElfEntersBattlefield() {
        setUpPoacher();
        harness.setLibrary(player1, List.of(new SkyshroudSentinel()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player1, "Skyshroud Sentinel");
    }

    @Test
    @DisplayName("Finding no Elf leaves the battlefield unchanged")
    void noElfFound() {
        setUpPoacher();
        harness.setLibrary(player1, List.of(new Mossdog()));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertNotOnBattlefield(player1, "Mossdog");
    }

    @Test
    @DisplayName("The ability requires three mana and taps Skyshroud Poacher")
    void activationRequiresThreeMana() {
        addCreatureReady(player1, new SkyshroudPoacher());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(findPermanent(player1, "Skyshroud Poacher").isTapped()).isFalse();

        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.setLibrary(player1, List.of(new Mossdog()));
        harness.activateAbility(player1, 0, null, null);

        assertThat(findPermanent(player1, "Skyshroud Poacher").isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
