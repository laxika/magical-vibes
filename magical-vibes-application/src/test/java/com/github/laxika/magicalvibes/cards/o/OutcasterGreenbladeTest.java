package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HostileDesert;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.LibrarySearchDestination;
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

@CardUsed({OutcasterGreenblade.class, Forest.class, HostileDesert.class, GrizzlyBears.class})
class OutcasterGreenbladeTest extends BaseCardTest {

    @Test
    @DisplayName("Entering searches for a basic land or Desert and puts it into hand")
    void enteringSearchesForBasicLandOrDesert() {
        Forest forest = new Forest();
        HostileDesert desert = new HostileDesert();
        harness.setLibrary(player1, List.of(forest, desert, new GrizzlyBears()));
        harness.setHand(player1, List.of(new OutcasterGreenblade()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().destination()).isEqualTo(LibrarySearchDestination.HAND);
        assertThat(search.params().cards()).contains(forest, desert);
        assertThat(search.params().cards()).allMatch(card -> card == forest || card == desert);

        Card chosen = search.params().cards().stream()
                .filter(card -> card.getName().equals("Hostile Desert"))
                .findFirst()
                .orElseThrow();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(search.params().cards().indexOf(chosen)));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Gets +1/+1 for each Desert its controller controls")
    void getsPlusOneForEachControlledDesert() {
        harness.addToBattlefield(player1, new OutcasterGreenblade());
        harness.addToBattlefield(player1, new HostileDesert());
        harness.addToBattlefield(player1, new HostileDesert());

        Permanent greenblade = findPermanent(player1, "Outcaster Greenblade");
        assertThat(gqs.getEffectivePower(gd, greenblade)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, greenblade)).isEqualTo(4);
    }

    @Test
    @DisplayName("Deserts controlled by an opponent do not contribute")
    void opponentDesertsDoNotContribute() {
        harness.addToBattlefield(player1, new OutcasterGreenblade());
        harness.addToBattlefield(player2, new HostileDesert());

        Permanent greenblade = findPermanent(player1, "Outcaster Greenblade");
        assertThat(gqs.getEffectivePower(gd, greenblade)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, greenblade)).isEqualTo(2);
    }
}
