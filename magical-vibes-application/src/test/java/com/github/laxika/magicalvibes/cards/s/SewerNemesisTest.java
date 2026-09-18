package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SewerNemesis.class, GrizzlyBears.class, SuntailHawk.class})
class SewerNemesisTest extends BaseCardTest {

    @Test
    @DisplayName("Power and toughness equal the chosen player's graveyard size")
    void powerAndToughnessCountChosenPlayersGraveyard() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        Permanent sewer = castSewerNemesis();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, player2.getId());

        assertThat(gqs.getEffectivePower(gd, sewer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sewer)).isEqualTo(2);

        gd.playerGraveyards.get(player2.getId()).add(new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, sewer)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sewer)).isEqualTo(3);
    }

    @Test
    @DisplayName("The chosen player mills one card when they cast a spell")
    void chosenPlayerMillsOnSpellCast() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        Permanent sewer = castSewerNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setLibrary(player2, libraryOfSize(10));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castCreature(player2, 0);
        assertThat(gd.stack).hasSize(2);
        int libraryBefore = gd.playerDecks.get(player2.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore - 1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(sewer.getRememberedTargetPlayerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("A different player's spell does not trigger the mill")
    void otherPlayerSpellDoesNotTrigger() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        castSewerNemesis();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
    }

    private List<com.github.laxika.magicalvibes.model.Card> libraryOfSize(int size) {
        var cards = new ArrayList<com.github.laxika.magicalvibes.model.Card>();
        for (int i = 0; i < size; i++) {
            cards.add(new SuntailHawk());
        }
        return cards;
    }

    private Permanent castSewerNemesis() {
        harness.setHand(player1, List.of(new SewerNemesis()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Sewer Nemesis");
    }
}
