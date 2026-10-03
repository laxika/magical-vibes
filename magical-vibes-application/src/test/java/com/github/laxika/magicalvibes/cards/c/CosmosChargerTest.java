package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AuguryRaven;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CosmosCharger.class, AuguryRaven.class, TurnToFrog.class})
class CosmosChargerTest extends BaseCardTest {

    @Test
    void reducesForetellCostAndAllowsForetellingOnAnOpponentsTurn() {
        Permanent charger = addCreatureReady(player1, new CosmosCharger());
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.foretell(player1, 0);

        ExiledCardEntry entry = gd.findExiledCard(raven.getId());
        assertThat(entry).isNotNull();
        assertThat(entry.faceDown()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(charger);
    }

    @Test
    void multipleChargersReduceForetellingToZeroWithoutAddingMana() {
        addCreatureReady(player1, new CosmosCharger());
        addCreatureReady(player1, new CosmosCharger());
        addCreatureReady(player1, new CosmosCharger());
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);

        harness.foretell(player1, 0);

        assertThat(gd.findExiledCard(raven.getId()).faceDown()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentsChargerDoesNotReduceYourForetellingCost() {
        addCreatureReady(player2, new CosmosCharger());
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(raven.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(raven);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.foretell(player1, 0);

        assertThat(gd.findExiledCard(raven.getId()).faceDown()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void opponentsChargerDoesNotAllowForetellingOnTheirTurn() {
        addCreatureReady(player2, new CosmosCharger());
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(raven.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(raven);
    }

    @Test
    void chargerInHandDoesNotReduceItsOwnForetellingCost() {
        CosmosCharger charger = new CosmosCharger();
        harness.setHand(player1, List.of(charger));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(charger.getId())).isNull();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.foretell(player1, 0);

        assertThat(gd.findExiledCard(charger.getId()).faceDown()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    void foretoldChargerCannotBeCastOnTheSameTurnEvenWithFlash() {
        CosmosCharger charger = new CosmosCharger();
        harness.setHand(player1, List.of(charger));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.foretell(player1, 0);

        assertThatThrownBy(() -> harness.castFromExile(player1, charger.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(charger.getId()).faceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void foretoldChargerCanBeCastOnALaterOpponentsTurnForItsFullForetellCost() {
        addCreatureReady(player1, new CosmosCharger());
        CosmosCharger charger = new CosmosCharger();
        harness.setHand(player1, List.of(charger));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, charger.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(charger.getId()).faceDown()).isTrue();

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castFromExile(player1, charger.getId());
        harness.passBothPriorities();

        assertThat(gd.findExiledCard(charger.getId())).isNull();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(charger.getId()));
    }

    @Test
    void foretellingPermissionDoesNotGiveForetoldRavenFlash() {
        addCreatureReady(player1, new CosmosCharger());
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.foretell(player1, 0);
        gd.turnNumber++;
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castFromExile(player1, raven.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(raven.getId()).faceDown()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void canForetellWithASpellOnTheStackWithoutChangingTheStack() {
        addCreatureReady(player1, new CosmosCharger());
        CosmosCharger spell = new CosmosCharger();
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(spell, raven));
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        assertThat(gd.stack).hasSize(1);
        var stackBeforeForetelling = List.copyOf(gd.stack);

        harness.foretell(player1, 0);

        assertThat(gd.findExiledCard(raven.getId()).faceDown()).isTrue();
        assertThat(gd.stack).containsExactlyElementsOf(stackBeforeForetelling);
    }

    @Test
    void losingAbilitiesRemovesForetellingCostReduction() {
        Permanent charger = addCreatureReady(player1, new CosmosCharger());
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, charger.getId());
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(raven.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(raven);
    }

    @Test
    void losingAbilitiesRemovesForetellingPermissionOnOpponentsTurn() {
        Permanent charger = addCreatureReady(player1, new CosmosCharger());
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, charger.getId());
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(raven.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(raven);
    }

    @Test
    void benefitsStopWhenChargerLeavesTheBattlefield() {
        Permanent charger = addCreatureReady(player1, new CosmosCharger());
        AuguryRaven raven = new AuguryRaven();
        harness.setHand(player1, List.of(raven));
        gd.playerBattlefields.get(player1.getId()).remove(charger);
        gd.playerGraveyards.get(player1.getId()).add(charger.getCard());
        harness.forceActivePlayer(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.forceActivePlayer(player1);
        assertThatThrownBy(() -> harness.foretell(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.findExiledCard(raven.getId())).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(raven);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.foretell(player1, 0);

        assertThat(gd.findExiledCard(raven.getId()).faceDown()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }
}
