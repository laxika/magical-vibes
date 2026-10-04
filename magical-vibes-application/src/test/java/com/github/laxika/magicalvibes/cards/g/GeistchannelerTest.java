package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.a.AcademyJourneymage;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.u.Unwind;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Geistchanneler.class, Divination.class, Opt.class, Island.class, Unwind.class,
        AcademyJourneymage.class})
class GeistchannelerTest extends BaseCardTest {

    @Test
    void choosesOneQualifyingSpellAndPerpetuallyReducesOnlyThatCard() {
        Divination chosen = new Divination();
        Divination unchosen = new Divination();
        Opt tooSmall = new Opt();

        harness.setHand(player1, List.of(new Geistchanneler(), chosen, unchosen, tooSmall));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PerpetualPowerToughnessChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualPowerToughnessChoice.class);
        assertThat(choice.validIndices()).containsExactly(0, 1);
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);

        harness.addMana(player1, ManaColor.BLUE, 1);
        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
    }

    @Test
    void doesNotPromptWithoutAQualifyingSpell() {
        harness.setHand(player1, List.of(new Geistchanneler(), new Opt()));
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void includesEligibleInstantsButExcludesCreaturesAndLands() {
        harness.setHand(player1, List.of(new Geistchanneler(), new Unwind(),
                new AcademyJourneymage(), new Island(), new Opt()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.PerpetualPowerToughnessChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PerpetualPowerToughnessChoice.class);
        assertThat(choice.validIndices()).containsExactly(0);
        assertThatThrownBy(() -> harness.handleCardChosen(player1, 3))
                .isInstanceOf(IllegalArgumentException.class);
        harness.handleCardChosen(player1, 0);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void repeatedReductionsStillRequireColoredManaAndKeepTheCardEligible() {
        harness.setHand(player1, List.of(new Geistchanneler(), new Geistchanneler(), new Divination()));
        harness.setLibrary(player1, List.of(new Island(), new Island()));

        for (int i = 0; i < 2; i++) {
            harness.addMana(player1, ManaColor.BLUE, 1);
            harness.addMana(player1, ManaColor.COLORLESS, 1);
            harness.castCreature(player1, 0);
            harness.passBothPriorities();
            harness.passBothPriorities();
            harness.handleCardChosen(player1, i == 0 ? 1 : 0);
        }

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Card is not playable");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.assertInGraveyard(player1, "Divination");
    }

    @Test
    void reductionPersistsAfterTheChosenSpellResolvesAndReturnsToHand() {
        Divination chosen = new Divination();
        harness.setHand(player1, List.of(new Geistchanneler(), chosen));
        harness.setLibrary(player1, List.of(new Island(), new Island(), new Island(), new Island()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.assertInGraveyard(player1, "Divination");

        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(chosen));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
        harness.assertInGraveyard(player1, "Divination");
    }
}
