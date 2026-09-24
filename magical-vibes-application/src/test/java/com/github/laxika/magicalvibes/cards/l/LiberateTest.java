package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.ArdentSoldier;
import com.github.laxika.magicalvibes.cards.c.CoastalTower;
import com.github.laxika.magicalvibes.cards.d.DromarTheBanisher;
import com.github.laxika.magicalvibes.cards.e.EmpressGalina;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.PendingExileReturn;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Liberate.class, ArdentSoldier.class, CoastalTower.class, EmpressGalina.class, DromarTheBanisher.class})
class LiberateTest extends BaseCardTest {

    private void addLiberateMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Exiles the target creature you control and schedules its return")
    void exilesTargetCreatureYouControl() {
        harness.setHand(player1, List.of(new Liberate()));
        harness.addToBattlefield(player1, new ArdentSoldier());
        addLiberateMana();

        UUID soldierId = harness.getPermanentId(player1, "Ardent Soldier");
        harness.castInstant(player1, 0, soldierId);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Ardent Soldier");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ardent Soldier"));
        assertThat(gd.getDelayedActions(PendingExileReturn.class))
                .anyMatch(action -> action.card().getName().equals("Ardent Soldier"));
    }

    @Test
    @DisplayName("Returns the exiled creature at the next end step under its owner's control")
    void returnsAtEndStep() {
        harness.setHand(player1, List.of(new Liberate()));
        harness.addToBattlefield(player1, new ArdentSoldier());
        addLiberateMana();

        UUID soldierId = harness.getPermanentId(player1, "Ardent Soldier");
        harness.castInstant(player1, 0, soldierId);
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Ardent Soldier");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getName().equals("Ardent Soldier"));
    }

    @Test
    @DisplayName("Returns a stolen creature under its owner's control")
    void returnsStolenCreatureToOwner() {
        Permanent stolen = addCreatureReady(player2, new DromarTheBanisher());
        Permanent empress = addCreatureReady(player1, new EmpressGalina());
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(empress), null, stolen.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getId().equals(stolen.getId()));

        harness.setHand(player1, List.of(new Liberate()));
        addLiberateMana();

        harness.castInstant(player1, 0, stolen.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(stolen.getId()));
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(card -> card.getName().equals("Dromar, the Banisher"));

        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(stolen.getId()));
        harness.assertOnBattlefield(player2, "Dromar, the Banisher");
    }

    @Test
    @DisplayName("Cannot target a creature controlled by an opponent")
    void cannotTargetOpponentCreature() {
        harness.setHand(player1, List.of(new Liberate()));
        harness.addToBattlefield(player2, new ArdentSoldier());
        addLiberateMana();

        UUID soldierId = harness.getPermanentId(player2, "Ardent Soldier");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, soldierId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.setHand(player1, List.of(new Liberate()));
        harness.addToBattlefield(player1, new CoastalTower());
        addLiberateMana();

        UUID towerId = harness.getPermanentId(player1, "Coastal Tower");
        assertThatThrownBy(() -> harness.castInstant(player1, 0, towerId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Returned creature is a new object with summoning sickness")
    void returnedCreatureHasSummoningSickness() {
        harness.setHand(player1, List.of(new Liberate()));
        harness.addToBattlefield(player1, new ArdentSoldier());
        addLiberateMana();

        UUID soldierId = harness.getPermanentId(player1, "Ardent Soldier");
        harness.castInstant(player1, 0, soldierId);
        harness.passBothPriorities();

        harness.passUntil(player1, TurnStep.END_STEP);

        Permanent returned = findPermanent(player1, "Ardent Soldier");
        assertThat(returned.getId()).isNotEqualTo(soldierId);
        assertThat(returned.isSummoningSick()).isTrue();
    }
}
