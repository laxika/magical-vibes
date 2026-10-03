package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
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
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BasrisAegis.class, BasriDevotedPaladin.class, GrizzlyBears.class, Plains.class})
class BasrisAegisTest extends BaseCardTest {

    @Test
    @DisplayName("Puts counters on up to two target creatures and finds Basri from the graveyard")
    void countersCreaturesAndFindsBasriFromGraveyard() {
        Permanent first = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new BasriDevotedPaladin()));
        cast(List.of(first.getId(), second.getId()));

        harness.passBothPriorities();
        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Basri, Devoted Paladin");
        harness.assertNotInGraveyard(player1, "Basri, Devoted Paladin");
    }

    @Test
    @DisplayName("Can choose one creature and decline the optional search")
    void canChooseOneCreatureAndDeclineSearch() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        cast(List.of(creature.getId()));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(creature.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Offers Basri from the library when the optional search is accepted")
    void searchesLibraryForBasri() {
        Card basri = new BasriDevotedPaladin();
        harness.setLibrary(player1, List.of(basri));
        cast(List.of());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(basri);

        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Basri, Devoted Paladin");
    }

    @Test
    @DisplayName("Rejects a non-creature target")
    void rejectsNonCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Plains());
        harness.setHand(player1, List.of(new BasrisAegis()));
        addMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of(land.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    private void cast(List<UUID> targets) {
        harness.setHand(player1, List.of(new BasrisAegis()));
        addMana();
        harness.castSorcery(player1, 0, targets);
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
    }
}
