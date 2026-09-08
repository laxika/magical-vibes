package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IntetTheDreamer.class, GrizzlyBears.class})
class IntetTheDreamerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {2}{U} exiles the top card of the controller's library face down with Intet")
    void payingExilesTopCardFaceDownWithIntet() {
        Permanent intet = addAttackingIntet();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatToMayPrompt();
        payForTrigger();

        assertThat(gd.getCardsExiledByPermanent(intet.getId())).containsExactly(topCard);
        assertThat(gd.findExiledCard(topCard.getId())).extracting(ExiledCardEntry::faceDown)
                .isEqualTo(true);
    }

    @Test
    @DisplayName("The card exiled by Intet can be cast without paying its mana cost")
    void castsExiledCardWithoutPayingManaCost() {
        Permanent intet = addAttackingIntet();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatToMayPrompt();
        payForTrigger();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromExile(player1, topCard.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.findExiledCard(topCard.getId())).isNull();
        assertThat(gd.getCardsExiledByPermanent(intet.getId())).isEmpty();
    }

    @Test
    @DisplayName("Declining the payment does not exile a card")
    void decliningPaymentDoesNotExile() {
        Permanent intet = addAttackingIntet();
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));

        resolveCombatToMayPrompt();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getCardsExiledByPermanent(intet.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(topCard);
    }

    private Permanent addAttackingIntet() {
        Permanent intet = addCreatureReady(player1, new IntetTheDreamer());
        intet.setAttacking(true);
        return intet;
    }

    private void resolveCombatToMayPrompt() {
        resolveCombat();
        harness.passBothPriorities();
    }

    private void payForTrigger() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
    }
}
