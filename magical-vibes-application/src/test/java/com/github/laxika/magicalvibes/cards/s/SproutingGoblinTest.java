package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CrystalGrotto;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NishobaBrawler;
import com.github.laxika.magicalvibes.cards.m.MoltenTributary;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SproutingGoblin.class, Forest.class, MoltenTributary.class, NishobaBrawler.class, CrystalGrotto.class})
class SproutingGoblinTest extends BaseCardTest {

    @Test
    void kickedEntrySearchesForLandWithBasicLandType() {
        harness.setHand(player1, List.of(new SproutingGoblin()));
        harness.setLibrary(player1, List.of(new MoltenTributary(), new Forest(), new NishobaBrawler()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(2)
                .anyMatch(MoltenTributary.class::isInstance)
                .anyMatch(Forest.class::isInstance)
                .noneMatch(NishobaBrawler.class::isInstance);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(
                card -> card instanceof MoltenTributary || card instanceof Forest);
    }

    @Test
    void unKickedEntryDoesNotSearch() {
        harness.setHand(player1, List.of(new SproutingGoblin()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        harness.assertOnBattlefield(player1, "Sprouting Goblin");
    }

    @Test
    void tappingAndSacrificingALandDrawsACard() {
        var goblin = addCreatureReady(player1, new SproutingGoblin());
        harness.addToBattlefield(player1, new Forest());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new NishobaBrawler()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(goblin.isTapped()).isTrue();
        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Nishoba Brawler");
    }

    @Test
    void abilityCannotBeActivatedWithoutALandToSacrifice() {
        addCreatureReady(player1, new SproutingGoblin());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void kickedSearchCanFailToFindEvenWithAnEligibleLand() {
        harness.setHand(player1, List.of(new SproutingGoblin()));
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void searchExcludesLandsWithoutABasicLandType() {
        harness.setHand(player1, List.of(new SproutingGoblin()));
        harness.setLibrary(player1, List.of(new CrystalGrotto(), new MoltenTributary()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        var search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(1).allMatch(MoltenTributary.class::isInstance);
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Molten Tributary");
        harness.assertNotInHand(player1, "Crystal Grotto");
        harness.assertNotOnBattlefield(player1, "Molten Tributary");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1)
                .allMatch(CrystalGrotto.class::isInstance);
    }

    @Test
    void kickedEntryWithNoEligibleLandCompletesWithoutAChoice() {
        harness.setHand(player1, List.of(new SproutingGoblin()));
        harness.setLibrary(player1, List.of(new NishobaBrawler()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Sprouting Goblin");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void summoningSicknessPreventsActivatingTheTapAbility() {
        harness.addToBattlefield(player1, new SproutingGoblin());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Forest");
    }

    @Test
    void sacrificeIsPaidBeforeTheDrawResolvesAndCanUseANonbasicLand() {
        var goblin = addCreatureReady(player1, new SproutingGoblin());
        harness.addToBattlefield(player1, new MoltenTributary());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new NishobaBrawler()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(goblin.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Molten Tributary");
        harness.assertNotOnBattlefield(player1, "Molten Tributary");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Nishoba Brawler");
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
