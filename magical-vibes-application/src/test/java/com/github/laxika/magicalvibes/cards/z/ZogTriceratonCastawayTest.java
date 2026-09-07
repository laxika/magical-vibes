package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZogTriceratonCastaway.class, GrizzlyBears.class, Mountain.class, Forest.class})
class ZogTriceratonCastawayTest extends BaseCardTest {

    @Test
    @DisplayName("ETB makes target creature unable to block this turn")
    void etbMakesTargetUnableToBlock() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new ZogTriceratonCastaway()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(target.isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Mountaincycling discards Zog and offers only Mountain cards")
    void mountaincyclingDiscardsAndOffersMountains() {
        harness.setHand(player1, List.of(new ZogTriceratonCastaway()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        List<Card> library = gd.playerDecks.get(player1.getId());
        library.clear();
        library.addAll(List.of(new Mountain(), new Forest(), new Mountain()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Zog, Triceraton Castaway");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .allMatch(card -> card instanceof Mountain)
                .hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Mountain");
    }
}
