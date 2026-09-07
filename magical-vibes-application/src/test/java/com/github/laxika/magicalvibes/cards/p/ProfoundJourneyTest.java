package com.github.laxika.magicalvibes.cards.p;

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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ProfoundJourney.class, GrizzlyBears.class, HolyDay.class})
class ProfoundJourneyTest extends BaseCardTest {

    @Test
    void returnsTargetPermanentFromGraveyardToBattlefield() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ProfoundJourney()));
        addProfoundJourneyMana();

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void cannotTargetNonPermanentCard() {
        Card target = new HolyDay();
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new ProfoundJourney()));
        addProfoundJourneyMana();

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void reboundOffersAnotherCastAtNextUpkeep() {
        Card firstTarget = new GrizzlyBears();
        Card secondTarget = new GrizzlyBears();
        ProfoundJourney card = new ProfoundJourney();
        harness.setGraveyard(player1, List.of(firstTarget, secondTarget));
        harness.setHand(player1, List.of(card));
        addProfoundJourneyMana();

        harness.castSorcery(player1, 0, firstTarget.getId());
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, secondTarget.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(card.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Profound Journey");
    }

    private void addProfoundJourneyMana() {
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
    }
}
