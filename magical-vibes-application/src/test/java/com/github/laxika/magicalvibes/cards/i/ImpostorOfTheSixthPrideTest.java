package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.FieldMarshal;
import com.github.laxika.magicalvibes.cards.k.KingOfThePride;
import com.github.laxika.magicalvibes.cards.r.ReturnFromExtinction;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ImpostorOfTheSixthPride.class, FieldMarshal.class, KingOfThePride.class,
        ReturnFromExtinction.class})
class ImpostorOfTheSixthPrideTest extends BaseCardTest {

    @Test
    @DisplayName("Changeling makes Impostor of the Sixth Pride a Soldier")
    void changelingMakesItASoldier() {
        harness.addToBattlefield(player1, new FieldMarshal());
        Permanent impostor = harness.addToBattlefieldAndReturn(player1, new ImpostorOfTheSixthPride());

        assertThat(gqs.getEffectivePower(gd, impostor)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, impostor)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, impostor, Keyword.FIRST_STRIKE)).isTrue();
    }

    @Test
    @DisplayName("Changeling shares the Cat type with another creature card in the graveyard")
    void changelingWorksInGraveyard() {
        var impostor = new ImpostorOfTheSixthPride();
        var cat = new KingOfThePride();
        harness.setGraveyard(player1, List.of(impostor, cat));
        harness.setHand(player1, List.of(new ReturnFromExtinction()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castModalSorcery(player1, 0, 1, List.of());

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(impostor.getId(), cat.getId());
        harness.handleMultipleCardsChosen(player1, List.of(impostor.getId(), cat.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Impostor of the Sixth Pride");
        harness.assertInHand(player1, "King of the Pride");
        harness.assertNotInGraveyard(player1, "Impostor of the Sixth Pride");
        harness.assertNotInGraveyard(player1, "King of the Pride");
    }
}
