package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SmugglersSurprise.class, Forest.class, GrizzlyBears.class, SerraAngel.class})
class SmugglersSurpriseTest extends BaseCardTest {

    @Test
    @DisplayName("The first mode mills four and can return up to two creature or land cards")
    void millsAndReturnsUpToTwoMatchingCards() {
        Card forest = new Forest();
        Card bears = new GrizzlyBears();
        setDeck(forest, bears, new GrizzlyBears(), new GrizzlyBears());
        cast(new SmugglersSurprise(), 0, 3);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .contains("Grizzly Bears");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .contains("Grizzly Bears");
    }

    @Test
    @DisplayName("The second mode puts up to two creature cards from hand onto the battlefield")
    void putsUpToTwoCreaturesFromHandOntoBattlefield() {
        SmugglersSurprise spell = new SmugglersSurprise();
        SerraAngel angel = new SerraAngel();
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(spell, angel, bears));
        cast(spell, 1, 6);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutUpToCardsFromHandOntoBattlefieldChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(angel.getId(), bears.getId()));

        harness.assertOnBattlefield(player1, "Serra Angel");
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("The third mode protects qualifying creatures and expires at end of turn")
    void protectsCreaturesWithPowerAtLeastFourUntilEndOfTurn() {
        Permanent angel = addCreatureReady(player1, new SerraAngel());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        cast(new SmugglersSurprise(), 2, 2);

        assertThat(gqs.hasKeyword(gd, angel, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, angel, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.hasKeyword(gd, angel, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    private void cast(SmugglersSurprise spell, int mode, int totalMana) {
        if (gd.playerHands.get(player1.getId()).stream().noneMatch(card -> card == spell)) {
            harness.setHand(player1, List.of(spell));
        }
        harness.addMana(player1, ManaColor.GREEN, totalMana);
        harness.castModalInstantWithModes(player1, 0, 1, 3, new int[]{mode}, List.of());
        harness.passBothPriorities();
    }

    private void setDeck(Card... cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(List.of(cards));
    }
}
