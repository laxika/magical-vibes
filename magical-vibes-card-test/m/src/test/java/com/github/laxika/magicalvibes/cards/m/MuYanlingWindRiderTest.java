package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MuYanlingWindRider.class, GrizzlyBears.class, SerraAngel.class})
class MuYanlingWindRiderTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with a 3/2 colorless Vehicle artifact token with flying and crew 1")
    void entersWithVehicleToken() {
        castMuYanling();

        Permanent vehicle = findPermanent(player1, "Vehicle");

        assertThat(vehicle.getCard().getPower()).isEqualTo(3);
        assertThat(vehicle.getCard().getToughness()).isEqualTo(2);
        assertThat(vehicle.getCard().getColors()).isEmpty();
        assertThat(vehicle.getCard().getSubtypes()).containsExactly(CardSubtype.VEHICLE);
        assertThat(vehicle.getCard().hasType(CardType.ARTIFACT)).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isFalse();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isTrue();

        Permanent crew = addCreatureReady(player1, new GrizzlyBears());
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, crew.getId());
        }
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Draws when one or more flying creatures deal combat damage")
    void flyingCombatDamageDrawsOnce() {
        addCreatureReady(player1, new MuYanlingWindRider());
        Permanent firstAttacker = addCreatureReady(player1, new SerraAngel());
        Permanent secondAttacker = addCreatureReady(player1, new SerraAngel());
        firstAttacker.setAttacking(true);
        secondAttacker.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(12);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Does not draw when a nonflying creature deals combat damage")
    void nonflyingCombatDamageDoesNotDraw() {
        addCreatureReady(player1, new MuYanlingWindRider());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A crewed Vehicle token draws a card when it deals combat damage")
    void crewedVehicleCombatDamageDraws() {
        castMuYanling();
        Permanent vehicle = findPermanent(player1, "Vehicle");
        Permanent pilot = findPermanent(player1, "Mu Yanling, Wind Rider");
        vehicle.setSummoningSick(false);
        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        if (gd.interaction.isAwaitingInput()) {
            harness.handlePermanentChosen(player1, pilot.getId());
        }
        harness.passBothPriorities();
        vehicle.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 17);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 1);
    }

    @Test
    @DisplayName("Only controlled Vehicles gain flying, and the grant ends when Mu Yanling leaves")
    void flyingGrantTracksControllerAndSource() {
        castMuYanling();
        Permanent vehicle = findPermanent(player1, "Vehicle");
        Permanent muYanling = findPermanent(player1, "Mu Yanling, Wind Rider");
        assertThat(gqs.hasKeyword(gd, muYanling, Keyword.FLYING)).isFalse();

        gd.playerBattlefields.get(player1.getId()).remove(vehicle);
        gd.playerBattlefields.get(player2.getId()).add(vehicle);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isFalse();

        gd.playerBattlefields.get(player2.getId()).remove(vehicle);
        gd.playerBattlefields.get(player1.getId()).add(vehicle);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(muYanling);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("An opponent's flying creature dealing combat damage does not trigger Mu Yanling")
    void opponentFlyingCombatDamageDoesNotDraw() {
        addCreatureReady(player1, new MuYanlingWindRider());
        Permanent attacker = addCreatureReady(player2, new SerraAngel());
        attacker.setAttacking(true);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        resolveCombat(player2);
        resolveAllTriggers();

        harness.assertLife(player1, 16);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.stack).isEmpty();
    }

    private void castMuYanling() {
        harness.setHand(player1, List.of(new MuYanlingWindRider()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
