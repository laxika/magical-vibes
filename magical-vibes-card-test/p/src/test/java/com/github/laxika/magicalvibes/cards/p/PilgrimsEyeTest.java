package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CreepingTarPit;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PilgrimsEye.class, Plains.class, Forest.class, Island.class,
        PerimeterCaptain.class, CreepingTarPit.class})
class PilgrimsEyeTest extends BaseCardTest {

    @Test
    @DisplayName("Accepting the ETB ability offers only basic lands and puts one into hand")
    void acceptingEtbSearchesForBasicLand() {
        castPilgrimsEye();
        setLibrary(new Plains(), new Forest(), new Island(), new PerimeterCaptain());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards())
                .allMatch(card -> card.hasType(CardType.LAND)
                        && card.getSupertypes().contains(CardSupertype.BASIC));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the ETB ability skips the library search")
    void decliningEtbSkipsSearch() {
        castPilgrimsEye();
        setLibrary(new Plains(), new PerimeterCaptain());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("A nonbasic land is excluded and the selected basic land is revealed and moved to hand")
    void excludesNonbasicLandAndRevealsChosenCard() {
        castPilgrimsEye();
        Plains plains = new Plains();
        CreepingTarPit nonbasic = new CreepingTarPit();
        setLibrary(nonbasic, plains, new PerimeterCaptain());

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactly(plains);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(plains);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).contains(nonbasic).doesNotContain(plains);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("reveals Plains"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The controller may fail to find even when a basic land is available")
    void mayFailToFindAvailableBasicLand() {
        castPilgrimsEye();
        Plains plains = new Plains();
        setLibrary(plains);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(plains);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching a library with no basic lands finishes without taking a card")
    void noBasicLandsInLibrary() {
        castPilgrimsEye();
        CreepingTarPit nonbasic = new CreepingTarPit();
        PerimeterCaptain creature = new PerimeterCaptain();
        setLibrary(nonbasic, creature);

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(nonbasic, creature);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled"));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Searching an empty library finishes normally")
    void emptyLibrary() {
        castPilgrimsEye();
        setLibrary();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castPilgrimsEye() {
        harness.setHand(player1, List.of(new PilgrimsEye()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
    }

    private void setLibrary(Card... cards) {
        harness.setLibrary(player1, List.of(cards));
    }
}
