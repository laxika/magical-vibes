package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SummoningStationTest extends BaseCardTest {

    @Test
    void createsAColorlessPincherToken() {
        Permanent station = addReadyStation(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(station.isTapped()).isTrue();
        Permanent token = findPermanent(player1, "Pincher");
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.PINCHER);
    }

    @Test
    void mayUntapWhenAnArtifactIsPutIntoAGraveyard() {
        Permanent station = addReadyStation(player1);
        station.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        destroyArtifact(artifact);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(station.isTapped()).isFalse();
    }

    @Test
    void decliningTheArtifactTriggerLeavesStationTapped() {
        Permanent station = addReadyStation(player1);
        station.tap();
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());

        destroyArtifact(artifact);

        harness.handleMayAbilityChosen(player1, false);

        assertThat(station.isTapped()).isTrue();
    }

    private Permanent addReadyStation(Player player) {
        Permanent station = harness.addToBattlefieldAndReturn(player, new SummoningStation());
        station.setSummoningSick(false);
        return station;
    }

    private void destroyArtifact(Permanent artifact) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Shatter()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, artifact.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
