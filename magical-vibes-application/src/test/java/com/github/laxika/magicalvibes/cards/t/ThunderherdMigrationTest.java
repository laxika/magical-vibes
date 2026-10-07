package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ThunderherdMigration.class, ColossalDreadmaw.class, Plains.class, Forest.class, Island.class})
class ThunderherdMigrationTest extends BaseCardTest {

    @Test
    @DisplayName("Without a Dinosaur in hand it requires the additional {1}")
    void requiresAdditionalManaWithoutDinosaur() {
        harness.setHand(player1, List.of(new ThunderherdMigration()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, List.of()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The additional {1} can be paid without revealing a Dinosaur")
    void paysAdditionalManaWithoutDinosaur() {
        ThunderherdMigration migration = new ThunderherdMigration();
        harness.setHand(player1, List.of(migration));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        setupLibrary();

        harness.castAndResolveSorcery(player1, 0, List.of());

        chooseFirstBasicLand();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(migration.getId()));
    }

    @Test
    @DisplayName("A Dinosaur in hand avoids the additional {1}")
    void revealDinosaurAvoidsAdditionalMana() {
        ThunderherdMigration migration = new ThunderherdMigration();
        ColossalDreadmaw dinosaur = new ColossalDreadmaw();
        harness.setHand(player1, List.of(migration, dinosaur));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        setupLibrary();

        harness.castAndResolveSorcery(player1, 0, List.of());

        chooseFirstBasicLand();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(dinosaur.getId()));
    }

    private void chooseFirstBasicLand() {
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.BATTLEFIELD_TAPPED);

        harness.handleCardChosen(player1, 0);
    }

    private void setupLibrary() {
        harness.setLibrary(player1, List.of(new Plains(), new Forest(), new Island(), new ColossalDreadmaw()));
    }

    @Test
    @DisplayName("Avoiding the additional mana publicly reveals the Dinosaur while casting")
    void revealsDinosaurBeforeResolution() {
        harness.setHand(player1, List.of(new ThunderherdMigration(), new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);

        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals")
                && entry.plainText().contains("Colossal Dreadmaw"));
        harness.assertInHand(player1, "Colossal Dreadmaw");
    }

    @Test
    @DisplayName("A Dinosaur outside the caster's hand cannot pay the reveal cost")
    void dinosaurOutsideOwnHandDoesNotAvoidAdditionalMana() {
        harness.setHand(player1, List.of(new ThunderherdMigration()));
        harness.setHand(player2, List.of(new ColossalDreadmaw()));
        harness.addToBattlefield(player1, new ColossalDreadmaw());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The chosen basic land enters tapped under the caster's control and the library is shuffled")
    void putsChosenLandOntoBattlefieldTapped() {
        Forest forest = new Forest();
        ColossalDreadmaw dinosaur = new ColossalDreadmaw();
        harness.setHand(player1, List.of(new ThunderherdMigration()));
        harness.setLibrary(player1, List.of(forest, dinosaur));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(card -> card.getId()).containsExactly(forest.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(forest.getId()) && permanent.isTapped());
        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(dinosaur);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("shuffled"));
        harness.assertInGraveyard(player1, "Thunderherd Migration");
    }

    @Test
    @DisplayName("The caster may fail to find even when a basic land is available")
    void mayFailToFindBasicLand() {
        Forest forest = new Forest();
        harness.setHand(player1, List.of(new ThunderherdMigration()));
        harness.setLibrary(player1, List.of(forest));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("shuffled"));
        harness.assertInGraveyard(player1, "Thunderherd Migration");
    }

    @Test
    @DisplayName("An empty library does not prevent resolution")
    void resolvesWithEmptyLibrary() {
        harness.setHand(player1, List.of(new ThunderherdMigration()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, List.of());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Thunderherd Migration");
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("shuffled"));
    }
}
