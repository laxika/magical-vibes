package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StoneriseSpiritTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles any card from the graveyard and grants flying to the target creature")
    void exilesAnyCardAndGrantsFlying() {
        harness.addToBattlefield(player1, new StoneriseSpirit());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();

        harness.activateAbility(player1, 0, null, bearsId);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.GraveyardExileCostChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertNotInGraveyard(player1, "Forest");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Forest"));

        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Flying granted by the ability expires at end of turn")
    void flyingExpiresAtEndOfTurn() {
        harness.addToBattlefield(player1, new StoneriseSpirit());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();

        harness.activateAbility(player1, 0, null, bearsId);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Grizzly Bears").hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without a card in the graveyard")
    void cannotActivateWithoutGraveyardCard() {
        harness.addToBattlefield(player1, new StoneriseSpirit());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        UUID bearsId = findPermanent(player1, "Grizzly Bears").getId();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("graveyard");
    }
}
