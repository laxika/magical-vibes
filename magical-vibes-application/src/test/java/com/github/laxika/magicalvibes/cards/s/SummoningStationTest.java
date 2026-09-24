package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.d.DevourInShadow;
import com.github.laxika.magicalvibes.cards.g.Granulate;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SummoningStation.class, ConjurersBauble.class, Granulate.class, DevourInShadow.class,
        SkyhunterProwler.class})
class SummoningStationTest extends BaseCardTest {

    @Test
    void createsAColorlessPincherToken() {
        Permanent station = addReadyStation(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(station.isTapped()).isTrue();
        Permanent token = findPermanent(player1, "Pincher");
        assertThat(token.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
        assertThat(token.getCard().getColor()).isNull();
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.PINCHER);
    }

    @Test
    void mayUntapWhenAnArtifactIsPutIntoAGraveyard() {
        Permanent station = addReadyStation(player1);
        station.tap();
        harness.addToBattlefield(player2, new ConjurersBauble());

        destroyArtifacts();

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
        harness.addToBattlefield(player2, new ConjurersBauble());

        destroyArtifacts();

        harness.handleMayAbilityChosen(player1, false);

        assertThat(station.isTapped()).isTrue();
    }

    @Test
    void createsOneMayTriggerForEachArtifactPutIntoAGraveyard() {
        Permanent station = addReadyStation(player1);
        station.tap();
        harness.addToBattlefield(player2, new ConjurersBauble());
        harness.addToBattlefield(player2, new ConjurersBauble());

        destroyArtifacts();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(station.isTapped()).isFalse();
    }

    @Test
    void doesNotTriggerWhenANonartifactCreatureDies() {
        Permanent station = addReadyStation(player1);
        station.tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player2, new SkyhunterProwler());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new DevourInShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, creature.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(station.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Skyhunter Prowler");
    }

    private Permanent addReadyStation(Player player) {
        Permanent station = harness.addToBattlefieldAndReturn(player, new SummoningStation());
        station.setSummoningSick(false);
        return station;
    }

    private void destroyArtifacts() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castFromHand(player1, new Granulate(), "{2}{R}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
