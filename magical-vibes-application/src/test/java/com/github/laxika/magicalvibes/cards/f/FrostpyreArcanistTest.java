package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AetherAdept;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class FrostpyreArcanistTest extends BaseCardTest {

    @Test
    @DisplayName("Costs {4}{U} without a Giant or Wizard")
    void costsFullManaWithoutGiantOrWizard() {
        harness.setHand(player1, List.of(new FrostpyreArcanist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs {3}{U} while controlling a Giant")
    void costsReducedManaWithGiant() {
        harness.addToBattlefield(player1, new HillGiant());
        harness.setHand(player1, List.of(new FrostpyreArcanist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Costs {3}{U} while controlling a Wizard")
    void costsReducedManaWithWizard() {
        harness.addToBattlefield(player1, new AetherAdept());
        harness.setHand(player1, List.of(new FrostpyreArcanist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("ETB search offers an instant or sorcery sharing a graveyard name")
    void etbSearchOffersMatchingInstantOrSorcery() {
        harness.setGraveyard(player1, List.of(new Shock()));
        setLibrary(new Shock(), new Opt(), new GrizzlyBears());
        castFrostpyreArcanist();

        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactly("Shock");
    }

    @Test
    @DisplayName("ETB search puts the chosen matching card into hand")
    void etbSearchPutsChosenCardIntoHand() {
        Shock shock = new Shock();
        harness.setGraveyard(player1, List.of(new Shock()));
        setLibrary(shock, new Opt());
        castFrostpyreArcanist();

        harness.passBothPriorities();
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Shock");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castFrostpyreArcanist() {
        harness.setHand(player1, List.of(new FrostpyreArcanist()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private void setLibrary(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
