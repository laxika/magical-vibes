package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.c.CagedSun;
import com.github.laxika.magicalvibes.cards.j.JackOLantern;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MoonsilverKey.class, Forest.class, MindStone.class, FountainOfYouth.class, GrizzlyBears.class, CagedSun.class, JackOLantern.class})
class MoonsilverKeyTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a mana-producing artifact or basic land")
    void searchesForManaProducingArtifactOrBasicLand() {
        harness.addToBattlefield(player1, new MoonsilverKey());
        harness.setLibrary(player1, List.of(new FountainOfYouth(), new Forest(), new MindStone(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card instanceof Forest || card instanceof MindStone)
                .anyMatch(card -> card instanceof Forest)
                .anyMatch(card -> card instanceof MindStone)
                .noneMatch(card -> card instanceof FountainOfYouth || card instanceof GrizzlyBears);

        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof Forest || card instanceof MindStone);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof MoonsilverKey);
    }

    @Test
    @DisplayName("Does not offer artifacts without mana abilities")
    void doesNotOfferArtifactsWithoutManaAbilities() {
        harness.addToBattlefield(player1, new MoonsilverKey());
        harness.setLibrary(player1, List.of(new FountainOfYouth(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card instanceof FountainOfYouth || card instanceof GrizzlyBears);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card instanceof MoonsilverKey);
    }

    @Test
    void findsArtifactWithTriggeredManaAbility() {
        harness.addToBattlefield(player1, new MoonsilverKey());
        harness.setLibrary(player1, List.of(new CagedSun()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(1).allMatch(card -> card instanceof CagedSun);
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Caged Sun");
    }

    @Test
    void findsArtifactWithGraveyardManaAbility() {
        harness.addToBattlefield(player1, new MoonsilverKey());
        harness.setLibrary(player1, List.of(new JackOLantern()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).hasSize(1).allMatch(card -> card instanceof JackOLantern);
        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Jack-o'-Lantern");
    }

    @Test
    void revealsFoundArtifactAndShufflesRemainingLibrary() {
        harness.addToBattlefield(player1, new MoonsilverKey());
        harness.setLibrary(player1, List.of(new MindStone(), new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        int index = java.util.stream.IntStream.range(0, search.params().cards().size())
                .filter(i -> search.params().cards().get(i) instanceof MindStone)
                .findFirst().orElseThrow();
        harness.handleCardChosen(player1, index);

        harness.assertInHand(player1, "Mind Stone");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1).allMatch(card -> card instanceof Forest);
        assertThat(gameLogContains("reveals Mind Stone")).isTrue();
        assertThat(gameLogContains("shuffled")).isTrue();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
    }
    @Test
    void mayFailToFindEvenWithEligibleCard() {
        harness.addToBattlefield(player1, new MoonsilverKey());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(card -> card instanceof MoonsilverKey);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, -1);

        harness.assertNotInHand(player1, "Forest");
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gameLogContains("Library is shuffled")).isTrue();
    }
}
