package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WightOfTheReliquary.class, GrizzlyBears.class, Forest.class, Shock.class})
class WightOfTheReliquaryTest extends BaseCardTest {

    @Test
    @DisplayName("Gets +1/+1 for each creature card in its controller's graveyard")
    void getsBoostFromCreatureCardsInGraveyard() {
        Permanent wight = addReadyWight(player1);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, wight)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, wight)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts only creature cards in its controller's graveyard")
    void ignoresNonCreaturesAndOpponentGraveyard() {
        Permanent wight = addReadyWight(player1);
        harness.setGraveyard(player1, List.of(new Forest(), new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears(), new GrizzlyBears()));

        assertThat(gqs.getEffectivePower(gd, wight)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, wight)).isEqualTo(3);
    }

    @Test
    @DisplayName("Sacrifices another creature and searches for a land onto the battlefield tapped")
    void sacrificesAnotherCreatureAndSearchesForTappedLand() {
        Permanent wight = addReadyWight(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Shock()));

        harness.activateAbility(player1, 0, null, null);

        assertThat(wight.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered).extracting(Card::getName).containsExactly("Forest");
        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate without another creature to sacrifice")
    void cannotActivateWithoutAnotherCreature() {
        addReadyWight(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyWight(Player player) {
        return addCreatureReady(player, new WightOfTheReliquary());
    }
}
