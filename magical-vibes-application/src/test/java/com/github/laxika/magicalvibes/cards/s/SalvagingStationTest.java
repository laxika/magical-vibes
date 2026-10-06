package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.ConjurersBauble;
import com.github.laxika.magicalvibes.cards.d.DevourInShadow;
import com.github.laxika.magicalvibes.cards.g.GuardianIdol;
import com.github.laxika.magicalvibes.cards.m.MyrServitor;
import com.github.laxika.magicalvibes.cards.p.ParadiseMantle;
import com.github.laxika.magicalvibes.cards.w.WayfarersBauble;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SalvagingStation.class, ConjurersBauble.class, WayfarersBauble.class, MyrServitor.class,
        GuardianIdol.class, SkyhunterProwler.class, DevourInShadow.class, ParadiseMantle.class})
class SalvagingStationTest extends BaseCardTest {

    @Test
    void returnsTargetNoncreatureArtifactWithManaValueOneOrLess() {
        harness.addToBattlefield(player1, new SalvagingStation());
        Card returnedCard = new WayfarersBauble();
        Card otherEligibleCard = new ConjurersBauble();
        Card creatureArtifact = new MyrServitor();
        Card expensiveArtifact = new GuardianIdol();
        harness.setGraveyard(player1, List.of(returnedCard, otherEligibleCard, creatureArtifact, expensiveArtifact));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(returnedCard.getId()));
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Wayfarer's Bauble");
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactly(otherEligibleCard, creatureArtifact, expensiveArtifact);
    }

    @Test
    void cannotTargetIneligibleArtifactOrOpponentGraveyard() {
        harness.addToBattlefield(player1, new SalvagingStation());
        Card creatureArtifact = new MyrServitor();
        Card expensiveArtifact = new GuardianIdol();
        Card opponentArtifact = new WayfarersBauble();
        harness.setGraveyard(player1, List.of(creatureArtifact, expensiveArtifact));
        harness.setGraveyard(player2, List.of(opponentArtifact));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(creatureArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(expensiveArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(opponentArtifact.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayUntapWhenAcreatureDies() {
        Permanent station = addReadyStation(player1);
        station.tap();
        harness.addToBattlefield(player2, new SkyhunterProwler());

        harness.setHand(player1, List.of(new DevourInShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Skyhunter Prowler"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(station.isTapped()).isFalse();
    }

    @Test
    void mayDeclineToUntapWhenAcreatureDies() {
        Permanent station = addReadyStation(player1);
        station.tap();
        harness.addToBattlefield(player2, new SkyhunterProwler());

        harness.setHand(player1, List.of(new DevourInShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player2, "Skyhunter Prowler"));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(station.isTapped()).isTrue();
    }

    @Test
    void returnsZeroManaArtifactUntappedAndPaysTapCost() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new SalvagingStation());
        Card artifact = new ParadiseMantle();
        harness.setGraveyard(player1, List.of(artifact));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId()));
        assertThat(station.isTapped()).isTrue();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Paradise Mantle");
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(artifact.getId()))
                .allSatisfy(permanent -> assertThat(permanent.isTapped()).isFalse());
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    void cannotActivateTappedStation() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new SalvagingStation());
        station.tap();
        Card artifact = new WayfarersBauble();
        harness.setGraveyard(player1, List.of(artifact));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(artifact.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetNonartifactCard() {
        harness.addToBattlefield(player1, new SalvagingStation());
        Card instant = new DevourInShadow();
        harness.setGraveyard(player1, List.of(instant));

        assertThatThrownBy(() -> harness.activateAbilityWithGraveyardTargets(
                player1, 0, 0, List.of(instant.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void mayUntapWhenItsControllersArtifactCreatureDies() {
        Permanent station = harness.addToBattlefieldAndReturn(player1, new SalvagingStation());
        station.tap();
        harness.addToBattlefield(player1, new MyrServitor());
        harness.setHand(player1, List.of(new DevourInShadow()));
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Myr Servitor"));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(station.isTapped()).isFalse();
        harness.assertInGraveyard(player1, "Myr Servitor");
    }
    @Test
    void doesNotReturnTargetThatAnotherStationAlreadyReturned() {
        harness.addToBattlefield(player1, new SalvagingStation());
        harness.addToBattlefield(player1, new SalvagingStation());
        Card artifact = new WayfarersBauble();
        harness.setGraveyard(player1, List.of(artifact));

        harness.activateAbilityWithGraveyardTargets(player1, 0, 0, List.of(artifact.getId()));
        harness.activateAbilityWithGraveyardTargets(player1, 1, 0, List.of(artifact.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> permanent.getCard().getId().equals(artifact.getId()))
                .hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }
    private Permanent addReadyStation(Player player) {
        Permanent station = harness.addToBattlefieldAndReturn(player, new SalvagingStation());
        station.setSummoningSick(false);
        return station;
    }
}
