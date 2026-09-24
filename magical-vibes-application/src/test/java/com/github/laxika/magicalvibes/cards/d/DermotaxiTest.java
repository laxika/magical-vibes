package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Dermotaxi.class, Forest.class, GrizzlyBears.class, HillGiant.class})
class DermotaxiTest extends BaseCardTest {

    @Test
    void entersChoosingACreatureFromAnyGraveyard() {
        Card land = new Forest();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(land));
        harness.setGraveyard(player2, List.of(creature));

        Permanent taxi = castTaxi();

        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());

        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));

        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(creature);
        assertThat(gd.getCardsExiledByPermanent(taxi.getId())).containsExactly(creature);
    }

    @Test
    void copiesTheImprintedCreatureAsAnArtifactVehicleUntilEndOfTurn() {
        Permanent taxi = enterTaxiWithCreature(new GrizzlyBears());
        Permanent firstCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent secondCreature = addCreatureReady(player1, new HillGiant());

        harness.activateAbility(player1, indexOf(taxi), null, null);
        harness.passBothPriorities();

        assertThat(firstCreature.isTapped()).isTrue();
        assertThat(secondCreature.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, taxi)).isTrue();
        assertThat(gqs.getEffectivePower(gd, taxi)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, taxi)).isEqualTo(2);
        assertThat(taxi.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(taxi.getCard().getSubtypes()).contains(CardSubtype.VEHICLE);

        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, taxi)).isFalse();
        assertThat(taxi.getCard().hasType(CardType.ARTIFACT)).isTrue();
    }

    private Permanent enterTaxiWithCreature(Card creature) {
        harness.setGraveyard(player2, List.of(creature));
        Permanent taxi = castTaxi();
        PendingInteraction.MultiGraveyardChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice.validCardIds()).containsExactly(creature.getId());
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        return taxi;
    }

    private Permanent castTaxi() {
        Card taxiCard = new Dermotaxi();
        harness.castFromHand(player1, taxiCard, "{2}");
        harness.passBothPriorities();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(taxiCard.getId()))
                .findFirst().orElseThrow();
    }

    private int indexOf(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
