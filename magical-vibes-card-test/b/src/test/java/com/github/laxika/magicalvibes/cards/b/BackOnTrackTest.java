package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.ClamorousIronclad;
import com.github.laxika.magicalvibes.cards.j.JibbirikOmnivore;
import com.github.laxika.magicalvibes.cards.l.LocustSpray;
import com.github.laxika.magicalvibes.cards.v.VenomsacLagac;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BackOnTrack.class, JibbirikOmnivore.class, ClamorousIronclad.class,
        LocustSpray.class, VenomsacLagac.class})
class BackOnTrackTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a creature and creates an enhanced Pilot token")
    void returnsCreatureAndCreatesPilot() {
        Card creature = new JibbirikOmnivore();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BackOnTrack()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Jibbirik Omnivore");
        assertThat(findPilot()).isNotNull();
        harness.assertNotInGraveyard(player1, "Jibbirik Omnivore");
    }

    @Test
    @DisplayName("Returns a Vehicle and the Pilot can crew it with its power bonus")
    void returnsVehicleAndPilotCanCrewIt() {
        Card vehicleCard = new ClamorousIronclad();
        harness.setGraveyard(player1, List.of(vehicleCard));
        harness.setHand(player1, List.of(new BackOnTrack()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, vehicleCard.getId());
        harness.passBothPriorities();

        Permanent vehicle = findPermanent(player1, "Clamorous Ironclad");
        Permanent pilot = findPilot();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature non-Vehicle card")
    void cannotTargetNonCreatureNonVehicleCard() {
        Card instant = new LocustSpray();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new BackOnTrack()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, instant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot cast without a graveyard target just to create a Pilot")
    void cannotCastWithoutTarget() {
        harness.setHand(player1, List.of(new BackOnTrack()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a creature in an opponent's graveyard")
    void cannotTargetOpponentsCreature() {
        Card creature = new JibbirikOmnivore();
        harness.setGraveyard(player2, List.of(creature));
        harness.setHand(player1, List.of(new BackOnTrack()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, creature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Creates no Pilot when its only target leaves the graveyard")
    void createsNoPilotWhenTargetLeavesGraveyard() {
        Card creature = new JibbirikOmnivore();
        harness.setGraveyard(player1, List.of(creature));
        harness.setHand(player1, List.of(new BackOnTrack()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, creature.getId());
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(creature));
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Jibbirik Omnivore");
        harness.assertNotOnBattlefield(player1, "Pilot");
        harness.assertInGraveyard(player1, "Back on Track");
    }

    @Test
    @DisplayName("The newly created Pilot can saddle a Mount without increasing its actual power")
    void pilotCanSaddleMount() {
        Card mountCard = new VenomsacLagac();
        harness.setGraveyard(player1, List.of(mountCard));
        harness.setHand(player1, List.of(new BackOnTrack()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castSorcery(player1, 0, mountCard.getId());
        harness.passBothPriorities();

        Permanent mount = findPermanent(player1, "Venomsac Lagac");
        Permanent pilot = findPilot();
        assertThat(pilot.getCard().isToken()).isTrue();
        assertThat(countPermanents(player1, "Pilot")).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, pilot)).isEqualTo(1);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(mount), null, null);
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isTrue();
        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
    }

    private Permanent findPilot() {
        return findPermanent(player1, "Pilot");
    }
}
