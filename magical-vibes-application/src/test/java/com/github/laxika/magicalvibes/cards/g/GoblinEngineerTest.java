package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.SolRing;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinEngineer.class, GolemsHeart.class, GrimMonolith.class, GrizzlyBears.class,
        SolRing.class, WurmcoilEngine.class})
class GoblinEngineerTest extends BaseCardTest {

    @Test
    @DisplayName("ETB may put an artifact from the library into the graveyard")
    void etbSearchesArtifactIntoGraveyard() {
        harness.setHand(player1, List.of(new GoblinEngineer()));
        Card libraryArtifact = new SolRing();
        harness.setLibrary(player1, List.of(libraryArtifact, new GrizzlyBears()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .extracting(Card::getId)
                .containsExactly(libraryArtifact.getId());

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(libraryArtifact.getId());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .doesNotContain(libraryArtifact.getId());
    }

    @Test
    @DisplayName("Activation sacrifices an artifact and returns a low mana-value artifact")
    void sacrificesArtifactAndReturnsArtifact() {
        var engineer = harness.addToBattlefieldAndReturn(player1, new GoblinEngineer());
        engineer.setSummoningSick(false);
        var sacrificedArtifact = harness.addToBattlefieldAndReturn(player1, new GolemsHeart());
        Card returnedArtifact = new GrimMonolith();
        harness.setGraveyard(player1, List.of(returnedArtifact));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, returnedArtifact.getId(), Zone.GRAVEYARD);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .contains(sacrificedArtifact.getCard().getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .contains(returnedArtifact.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(sacrificedArtifact.getCard().getId()));
    }

    @Test
    @DisplayName("Cannot target an artifact with mana value greater than 3")
    void rejectsArtifactAboveManaValueThree() {
        var engineer = harness.addToBattlefieldAndReturn(player1, new GoblinEngineer());
        engineer.setSummoningSick(false);
        harness.addToBattlefieldAndReturn(player1, new GolemsHeart());
        Card highManaValueArtifact = new WurmcoilEngine();
        harness.setGraveyard(player1, List.of(highManaValueArtifact));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null,
                highManaValueArtifact.getId(), Zone.GRAVEYARD))
                .isInstanceOf(IllegalStateException.class);
    }
}
