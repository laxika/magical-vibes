package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GolemsHeart;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoulFoundryTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers to exile a creature card from hand")
    void etbOffersCreatureImprint() {
        harness.setHand(player1, List.of(new SoulFoundry(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }

    @Test
    @DisplayName("Accepting ETB imprint exiles and imprints the creature card")
    void acceptsCreatureImprint() {
        harness.setHand(player1, List.of(new SoulFoundry(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        GameData gameData = harness.getGameData();
        assertThat(gameData.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Grizzly Bears"));
        assertThat(findPermanent(player1, "Soul Foundry")).satisfies(foundry ->
                assertThat(gameData.getImprintedCard(foundry.getCard()).getName()).isEqualTo("Grizzly Bears"));
    }

    @Test
    @DisplayName("Noncreature cards cannot be imprinted")
    void noncreatureCardsCannotBeImprinted() {
        harness.setHand(player1, List.of(new SoulFoundry(), new GolemsHeart()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInHand(player1, "Golem's Heart");
        assertThat(gd.getImprintedCard(findPermanent(player1, "Soul Foundry").getCard())).isNull();
    }

    @Test
    @DisplayName("Activated ability creates a permanent token copy of the imprinted creature")
    void createsPermanentTokenCopy() {
        SoulFoundry foundryCard = new SoulFoundry();
        GrizzlyBears imprintedCard = new GrizzlyBears();
        gd.setImprintedCard(foundryCard, imprintedCard);
        harness.addToBattlefield(player1, foundryCard);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, 2, null);
        harness.passBothPriorities();

        GameData gameData = harness.getGameData();
        Permanent token = gameData.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Grizzly Bears") && permanent.getCard().isToken())
                .findFirst().orElse(null);
        assertThat(token).isNotNull();
        assertThat(token.getCard().getPower()).isEqualTo(2);
        assertThat(token.getCard().getToughness()).isEqualTo(2);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gameData.playerBattlefields.get(player1.getId()))
                .contains(token);
    }

    @Test
    @DisplayName("Cannot activate without an imprinted card or with the wrong X")
    void validatesImprintedCardAndX() {
        SoulFoundry emptyFoundry = new SoulFoundry();
        harness.addToBattlefield(player1, emptyFoundry);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("No card has been exiled with");

        SoulFoundry foundryCard = new SoulFoundry();
        GrizzlyBears imprintedCard = new GrizzlyBears();
        gd.setImprintedCard(foundryCard, imprintedCard);
        harness.addToBattlefield(player1, foundryCard);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, 3, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("X must equal the mana value of the imprinted card");
    }
}
