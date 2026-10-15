package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.MarchesasSurpriseParty;
import com.github.laxika.magicalvibes.cards.t.ThrasiosTritonHero;
import com.github.laxika.magicalvibes.cards.t.TymnaTheWeaver;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CommandBeacon.class, ThrasiosTritonHero.class, TymnaTheWeaver.class,
        MarchesasSurpriseParty.class})
class CommandBeaconTest extends BaseCardTest {

    @Test
    void tapsForColorlessMana() {
        Permanent beacon = harness.addToBattlefieldAndReturn(player1, new CommandBeacon());

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(beacon.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void sacrificesItselfAndPutsACommanderIntoHand() {
        harness.addToBattlefield(player1, new CommandBeacon());
        Card commander = new ThrasiosTritonHero();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).add(commander);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.assertInGraveyard(player1, "Command Beacon");
        assertThat(gd.playerCommandZones.get(player1.getId())).contains(commander);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(commander);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Command Beacon");
        harness.assertInGraveyard(player1, "Command Beacon");
        assertThat(gd.playerHands.get(player1.getId())).contains(commander);
        assertThat(gd.playerCommandZones.get(player1.getId())).doesNotContain(commander);
    }

    @Test
    void choosesOneCommanderWhenMultipleAreInTheCommandZone() {
        harness.addToBattlefield(player1, new CommandBeacon());
        Card firstCommander = new ThrasiosTritonHero();
        Card secondCommander = new TymnaTheWeaver();
        gd.makeCommander(player1.getId(), firstCommander);
        gd.playerCommanders.get(player1.getId()).add(secondCommander);
        gd.playerCommandZones.get(player1.getId()).addAll(List.of(firstCommander, secondCommander));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.CommandZoneCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(secondCommander.getId()));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(secondCommander).doesNotContain(firstCommander);
        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(firstCommander);
    }

    @Test
    void sacrificesBeaconEvenWhenItsControllerHasNoCommanderInTheCommandZone() {
        harness.addToBattlefield(player1, new CommandBeacon());
        Card opponentCommander = new ThrasiosTritonHero();
        gd.makeCommander(player2.getId(), opponentCommander);
        gd.playerCommandZones.get(player2.getId()).add(opponentCommander);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Command Beacon");
        assertThat(gd.playerCommandZones.get(player2.getId())).containsExactly(opponentCommander);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(opponentCommander);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void cannotSacrificeBeaconAfterTappingItForMana() {
        harness.addToBattlefield(player1, new CommandBeacon());
        harness.activateAbility(player1, 0, 0, null, null);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Command Beacon");
        harness.assertNotInGraveyard(player1, "Command Beacon");
    }

    @Test
    void cannotMoveAConspiracyIntoHandWhenNoCommanderIsInTheCommandZone() {
        harness.addToBattlefield(player1, new CommandBeacon());
        Card conspiracy = new MarchesasSurpriseParty();
        gd.playerCommandZones.get(player1.getId()).add(conspiracy);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(conspiracy);
        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(conspiracy);
        harness.assertInGraveyard(player1, "Command Beacon");
    }

    @Test
    void ignoresAConspiracyWhenChoosingACommander() {
        harness.addToBattlefield(player1, new CommandBeacon());
        Card commander = new ThrasiosTritonHero();
        Card conspiracy = new MarchesasSurpriseParty();
        gd.makeCommander(player1.getId(), commander);
        gd.playerCommandZones.get(player1.getId()).addAll(List.of(commander, conspiracy));

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.CommanderReplacementChoice.class);
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerHands.get(player1.getId())).contains(commander).doesNotContain(conspiracy);
        assertThat(gd.playerCommandZones.get(player1.getId())).containsExactly(conspiracy);
    }
}
