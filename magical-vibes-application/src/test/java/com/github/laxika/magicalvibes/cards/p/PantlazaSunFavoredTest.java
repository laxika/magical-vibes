package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RaptorCompanion;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DisplayName("Pantlaza, Sun-Favored")
@CardUsed({PantlazaSunFavored.class, RaptorCompanion.class, GrizzlyBears.class, LlanowarElves.class})
class PantlazaSunFavoredTest extends BaseCardTest {

    @Test
    @DisplayName("Discovers using the entering Dinosaur's toughness")
    void discoversUsingEnteringDinosaurToughness() {
        addCreatureReady(player1, new PantlazaSunFavored());
        LlanowarElves discovered = new LlanowarElves();
        harness.setLibrary(player1, List.of(discovered));
        harness.setHand(player1, List.of(new RaptorCompanion()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(discovered);
    }

    @Test
    @DisplayName("Does not trigger for a non-Dinosaur creature")
    void doesNotTriggerForNonDinosaur() {
        addCreatureReady(player1, new PantlazaSunFavored());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
