package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Necropanther.class, GrizzlyBears.class, AirElemental.class, Opt.class})
class NecropantherTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating returns a target creature with mana value 3 or less from your graveyard")
    void mutatingReturnsTargetSmallCreature() {
        Permanent panther = addCreatureReady(player1, new Necropanther());
        Card eligible = new GrizzlyBears();
        Card tooExpensive = new AirElemental();
        Card nonCreature = new Opt();
        harness.setGraveyard(player1, List.of(eligible, tooExpensive, nonCreature));

        triggerMutation(panther);

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(eligible.getId());

        harness.handleMultipleCardsChosen(player1, List.of(eligible.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(eligible.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(tooExpensive.getId(), nonCreature.getId());
    }

    @Test
    @DisplayName("Mutating does not return an ineligible or opposing graveyard creature")
    void mutatingHasNoTargetWithoutQualifyingOwnCreature() {
        Permanent panther = addCreatureReady(player1, new Necropanther());
        Card tooExpensive = new AirElemental();
        Card nonCreature = new Opt();
        Card opposingCreature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(tooExpensive, nonCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));

        triggerMutation(panther);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class)).isNull();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(tooExpensive, nonCreature);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
    }

    private void triggerMutation(Permanent panther) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, panther, List.of(panther.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
