package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SilkwingScout.class, Plains.class, GrizzlyBears.class})
class SilkwingScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Silkwing Scout sacrifices it and puts the ability on the stack")
    void activatingSacrificesAndPutsOnStack() {
        addScout();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Silkwing Scout");
        harness.assertInGraveyard(player1, "Silkwing Scout");
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("The ability searches for a basic land and puts it onto the battlefield tapped")
    void searchesForBasicLandAndPutsItTapped() {
        activateSearch(List.of(new Plains(), new GrizzlyBears()));

        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).hasSize(1);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof Plains && permanent.isTapped());
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The ability can fail to find a basic land")
    void canFailToFindBasicLand() {
        activateSearch(List.of(new GrizzlyBears()));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability cannot be activated without green mana")
    void cannotActivateWithoutGreenMana() {
        addScout();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private void addScout() {
        harness.addToBattlefield(player1, new SilkwingScout());
    }

    private void activateSearch(List<Card> library) {
        addScout();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.setLibrary(player1, library);

        harness.activateAbility(player1, 0, null, null);
    }
}
