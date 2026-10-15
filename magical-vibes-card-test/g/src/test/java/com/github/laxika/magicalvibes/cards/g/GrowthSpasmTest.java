package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.e.EldraziTemple;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.n.NestInvader;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GrowthSpasm.class, Forest.class, NestInvader.class, EldraziTemple.class})
class GrowthSpasmTest extends BaseCardTest {

    @Test
    @DisplayName("Searches for a basic land tapped and creates an Eldrazi Spawn")
    void searchesForBasicLandAndCreatesSpawn() {
        harness.setLibrary(player1, List.of(new Forest(), new NestInvader()));
        castGrowthSpasm();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .singleElement()
                .satisfies(card -> assertThat(card.hasType(CardType.LAND)).isTrue());
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
    }

    @Test
    @DisplayName("The created Eldrazi Spawn can be sacrificed for colorless mana")
    void spawnCanBeSacrificedForColorlessMana() {
        harness.setLibrary(player1, List.of(new Forest()));
        castGrowthSpasm();
        harness.handleCardChosen(player1, 0);

        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        int spawnIndex = gd.playerBattlefields.get(player1.getId()).indexOf(spawn);
        harness.activateAbility(player1, spawnIndex, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
    }

    @Test
    @DisplayName("Creates a Spawn even when the library is empty")
    void createsSpawnWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        castGrowthSpasm();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
        Permanent spawn = findPermanent(player1, "Eldrazi Spawn");
        assertThat(spawn.getCard().isToken()).isTrue();
        assertThat(spawn.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(spawn.getCard().getColors()).isEmpty();
        assertThat(spawn.getCard().getSubtypes()).containsExactlyInAnyOrder(CardSubtype.ELDRAZI, CardSubtype.SPAWN);
        assertThat(gqs.getEffectivePower(gd, spawn)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, spawn)).isEqualTo(1);
        assertThat(spawn.isTapped()).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Growth Spasm");
    }

    @Test
    @DisplayName("Nonbasic lands and creatures cannot be found, but a Spawn is still created")
    void createsSpawnWhenNoBasicLandExists() {
        harness.setLibrary(player1, List.of(new EldraziTemple(), new NestInvader()));
        castGrowthSpasm();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        assertThat(findPermanents(player1, "Eldrazi Temple")).isEmpty();
        assertThat(findPermanents(player1, "Nest Invader")).isEmpty();
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("May fail to find an available basic land and still creates a Spawn")
    void createsSpawnAfterFailingToFind() {
        harness.setLibrary(player1, List.of(new Forest()));
        castGrowthSpasm();

        assertThat(findPermanents(player1, "Eldrazi Spawn")).isEmpty();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(findPermanents(player1, "Forest")).isEmpty();
        assertThat(findPermanents(player1, "Eldrazi Spawn")).hasSize(1);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertInGraveyard(player1, "Growth Spasm");
    }

    private void castGrowthSpasm() {
        harness.setHand(player1, List.of(new GrowthSpasm()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castAndResolveSorcery(player1, 0, 0);
    }
}
