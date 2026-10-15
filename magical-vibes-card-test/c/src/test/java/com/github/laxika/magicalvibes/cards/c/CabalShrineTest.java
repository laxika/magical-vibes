package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DarkwaterEgg;
import com.github.laxika.magicalvibes.cards.d.DiligentFarmhand;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CabalShrine.class, DarkwaterEgg.class, DiligentFarmhand.class, Forest.class, Island.class})
class CabalShrineTest extends BaseCardTest {

    @Test
    @DisplayName("The caster discards for same-name cards in all graveyards")
    void casterDiscardsForSameNameCardsInAllGraveyards() {
        harness.addToBattlefield(player1, new CabalShrine());
        harness.setGraveyard(player1, List.of(new DiligentFarmhand()));
        harness.setGraveyard(player2, List.of(new DiligentFarmhand(), new Island()));
        harness.setHand(player1,
                new ArrayList<>(List.of(new DiligentFarmhand(), new Forest(), new Island())));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Island");
    }

    @Test
    @DisplayName("An opponent casting a spell makes that opponent discard")
    void opponentCastingDiscards() {
        harness.addToBattlefield(player1, new CabalShrine());
        harness.setGraveyard(player1, List.of(new DiligentFarmhand()));
        harness.setHand(player2, new ArrayList<>(List.of(new DiligentFarmhand(), new Forest())));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());
        harness.handleCardChosen(player2, 0);

        harness.assertInGraveyard(player2, "Forest");
    }

    @Test
    @DisplayName("A spell with no same-name graveyard cards causes no discard")
    void noMatchingCardsCauseNoDiscard() {
        harness.addToBattlefield(player1, new CabalShrine());
        harness.setGraveyard(player1, List.of(new Island()));
        harness.setHand(player1, new ArrayList<>(List.of(new DiligentFarmhand(), new Forest())));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("A noncreature spell also causes its caster to discard")
    void noncreatureSpellAlsoCausesDiscard() {
        harness.addToBattlefield(player1, new CabalShrine());
        harness.setGraveyard(player1, List.of(new DarkwaterEgg()));
        harness.setHand(player1, new ArrayList<>(List.of(new DarkwaterEgg(), new Forest())));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    void countsCardsAddedToGraveyardsBeforeResolution() {
        harness.addToBattlefield(player1, new CabalShrine());
        harness.setHand(player1, List.of(new DiligentFarmhand(), new Forest(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.setGraveyard(player2, List.of(new DiligentFarmhand()));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player1, 0);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertInHand(player1, "Island");
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotCountCardsRemovedFromGraveyardsBeforeResolution() {
        harness.addToBattlefield(player1, new CabalShrine());
        harness.setGraveyard(player2, List.of(new DiligentFarmhand()));
        harness.setHand(player1, List.of(new DiligentFarmhand(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.setGraveyard(player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void discardsAvailableCardsWhenCountExceedsHandSize() {
        harness.addToBattlefield(player1, new CabalShrine());
        harness.setGraveyard(player2, List.of(new DiligentFarmhand(), new DiligentFarmhand()));
        harness.setHand(player1, List.of(new DiligentFarmhand(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Forest");
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Diligent Farmhand");
    }

    @Test
    void eachShrineCountsCardsDiscardedByThePreviousTrigger() {
        harness.addToBattlefield(player1, new CabalShrine());
        harness.addToBattlefield(player1, new CabalShrine());
        harness.setGraveyard(player2, List.of(new DiligentFarmhand()));
        harness.setHand(player1, List.of(new DiligentFarmhand(), new DiligentFarmhand(),
                new Forest(), new Island()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Diligent Farmhand");
        harness.assertInGraveyard(player1, "Forest");
        harness.assertInGraveyard(player1, "Island");
    }
}
