package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({UnderTheSkin.class, GrizzlyBears.class, Forest.class, HolyDay.class})
class UnderTheSkinTest extends BaseCardTest {

    @Test
    void manifestsDreadThenMayReturnPermanentFromGraveyard() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new UnderTheSkin()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class))
                .isNotNull();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isManifested()
                        && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(graveyardCard);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class))
                .isNotNull();

        harness.handleMayAbilityChosen(player1, true);
        harness.handleGraveyardCardChosen(player1, 0);

        harness.assertInHand(player1, "Forest");
        harness.assertNotInGraveyard(player1, "Forest");
    }

    @Test
    void mayReturnOnlyPermanentCards() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        Card nonPermanentCard = new HolyDay();
        harness.setHand(player1, List.of(new UnderTheSkin()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.setGraveyard(player1, List.of(nonPermanentCard));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.GraveyardChoice.class))
                .isNotNull();
        harness.handleGraveyardCardChosen(player1, 1);

        harness.assertInGraveyard(player1, "Holy Day");
        harness.assertInHand(player1, "Forest");
    }

    @Test
    void mayReturnCanBeDeclined() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new UnderTheSkin()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Forest");
        harness.assertNotInHand(player1, "Forest");
    }
}
