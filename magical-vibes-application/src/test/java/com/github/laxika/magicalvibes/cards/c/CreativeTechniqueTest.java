package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.ArcaneDenial;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CreativeTechnique.class, Forest.class, GrizzlyBears.class, ArcaneDenial.class})
class CreativeTechniqueTest extends BaseCardTest {

    @Test
    void revealsUntilNonlandAndCastsTheCardForFree() {
        harness.setLibrary(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.castFromHand(player1, new CreativeTechnique(), "{4}{R}");
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard() instanceof GrizzlyBears
                && entry.getEntryType() == StackEntryType.CREATURE_SPELL);
        assertThat(gd.playerManaPools.get(player1.getId()).getTotalAllMana()).isZero();
    }

    @Test
    void demonstrateCreatesCopiesForControllerAndChosenOpponent() {
        harness.castFromHand(player1, new CreativeTechnique(), "{4}{R}");
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack).filteredOn(StackEntry::isCopy).hasSize(2)
                .extracting(StackEntry::getControllerId)
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());
    }

    @Test
    void declinedNonlandCardRemainsInExile() {
        CreativeTechnique revealed = new CreativeTechnique();
        harness.setLibrary(player1, List.of(revealed));
        harness.castFromHand(player1, new CreativeTechnique(), "{4}{R}");
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.findExiledCard(revealed.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void nonlandIsExiledBeforeTheCastingDecision() {
        CreativeTechnique revealed = new CreativeTechnique();
        harness.setLibrary(player1, List.of(revealed));
        harness.castFromHand(player1, new CreativeTechnique(), "{4}{R}");
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(revealed.getId())).isNotNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryResolvesWithoutACastingChoice() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new CreativeTechnique(), "{4}{R}");
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Creative Technique");
    }

    @Test
    void allLandLibraryKeepsEveryCardAndOffersNoCast() {
        Forest first = new Forest();
        Forest second = new Forest();
        harness.setLibrary(player1, List.of(first, second));
        harness.castFromHand(player1, new CreativeTechnique(), "{4}{R}");
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void demonstrateStillCopiesTheSpellAfterItIsCountered() {
        CreativeTechnique original = new CreativeTechnique();
        harness.castFromHand(player1, original, "{4}{R}");
        harness.handleMayAbilityChosen(player1, true);
        harness.setHand(player2, List.of(new ArcaneDenial()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player2, 0, original.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Creative Technique");

        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(2).allMatch(StackEntry::isCopy)
                .extracting(StackEntry::getControllerId)
                .containsExactlyInAnyOrder(player1.getId(), player2.getId());
    }
}
