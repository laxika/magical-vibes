package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RekindlingPhoenixTest extends BaseCardTest {

    @Test
    @DisplayName("Dying creates a 0/1 red Elemental token")
    void dyingCreatesElementalToken() {
        killPhoenix();

        List<Permanent> elementals = findPermanents(player1, "Elemental");
        assertThat(elementals).hasSize(1);
        assertThat(elementals.getFirst().getCard().isToken()).isTrue();
        assertThat(elementals.getFirst().getEffectivePower()).isEqualTo(0);
        assertThat(elementals.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Elemental upkeep ability sacrifices the token and returns a Phoenix with haste")
    void tokenUpkeepAbilityReturnsPhoenixWithHaste() {
        killPhoenix();
        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice = (PendingInteraction.MultiGraveyardChoice)
                gd.interaction.activeInteraction();
        assertThat(choice).isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(choice.cards().getFirst().getId()));

        harness.assertNotOnBattlefield(player1, "Elemental");
        Permanent phoenix = findPermanents(player1, "Rekindling Phoenix").getFirst();
        assertThat(phoenix.hasKeyword(Keyword.HASTE)).isTrue();
        harness.assertNotInGraveyard(player1, "Rekindling Phoenix");
    }

    @Test
    @DisplayName("Elemental upkeep ability cannot return a non-Phoenix card")
    void tokenUpkeepAbilityCannotTargetOtherCard() {
        killPhoenix();
        Card otherCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(otherCreature));

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertOnBattlefield(player1, "Elemental");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    private void killPhoenix() {
        harness.addToBattlefield(player1, new RekindlingPhoenix());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new WrathOfGod()));
        harness.addMana(player2, ManaColor.WHITE, 4);
        harness.getGameService().playCard(harness.getGameData(), player2, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
