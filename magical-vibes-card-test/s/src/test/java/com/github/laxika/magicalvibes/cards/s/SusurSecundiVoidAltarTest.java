package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SusurSecundiVoidAltar.class, StationMonitor.class})
class SusurSecundiVoidAltarTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield tapped")
    void entersTapped() {
        harness.setHand(player1, List.of(new SusurSecundiVoidAltar()));

        harness.playLand(player1, 0);

        assertThat(findPermanent(player1, "Susur Secundi, Void Altar").isTapped()).isTrue();
    }

    @Test
    @DisplayName("Tap ability adds black mana")
    void tapAbilityAddsBlackMana() {
        Permanent altar = addAltarReady();

        harness.activateAbility(player1, battlefieldIndex(altar), 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(1);
        assertThat(altar.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Station adds charge counters equal to another creature's power")
    void stationUsesAnotherCreaturePower() {
        Permanent altar = addAltarReady();
        Permanent creature = addCreatureReady(player1, new StationMonitor());

        harness.activateAbility(player1, battlefieldIndex(altar), 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(altar.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing a creature draws cards equal to its power and costs two life")
    void sacrificeAbilityDrawsForSacrificedPower() {
        Permanent altar = addAltarReady();
        altar.setCounterCount(CounterType.CHARGE, 12);
        Permanent creature = addCreatureReady(player1, new StationMonitor());
        harness.setLibrary(player1, List.of(new StationMonitor(), new StationMonitor(), new StationMonitor()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int lifeBefore = gd.getLife(player1.getId());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, battlefieldIndex(altar), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(altar.isTapped()).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(creature.getCard().getId());
    }

    @Test
    @DisplayName("The sacrifice ability can only be activated at sorcery speed")
    void sacrificeAbilityRequiresSorcerySpeed() {
        Permanent altar = addAltarReady();
        altar.setCounterCount(CounterType.CHARGE, 12);
        addCreatureReady(player1, new StationMonitor());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(altar), 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("The draw ability is unavailable below twelve charge counters")
    void drawAbilityRequiresTwelveChargeCounters() {
        Permanent altar = addAltarReady();
        altar.setCounterCount(CounterType.CHARGE, 11);
        Permanent creature = addCreatureReady(player1, new StationMonitor());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(altar), 2, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(altar.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Station uses the creature's power at resolution")
    void stationUsesPowerAtResolution() {
        Permanent altar = addAltarReady();
        Permanent creature = addCreatureReady(player1, new StationMonitor());

        harness.activateAbility(player1, battlefieldIndex(altar), 1, null, null);
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.passBothPriorities();

        assertThat(altar.getCounterCount(CounterType.CHARGE)).isEqualTo(5);
        assertThat(altar.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Station may tap a summoning-sick creature while the Planet is tapped")
    void stationDoesNotRequireReadyCreatureOrUntappedPlanet() {
        Permanent altar = addAltarReady();
        altar.tap();
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new StationMonitor());
        creature.setSummoningSick(true);

        harness.activateAbility(player1, battlefieldIndex(altar), 1, null, null);
        harness.passBothPriorities();

        assertThat(creature.isTapped()).isTrue();
        assertThat(altar.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Station cannot use a tapped creature or an opponent's creature")
    void stationRequiresUntappedCreatureYouControl() {
        Permanent altar = addAltarReady();
        Permanent creature = addCreatureReady(player1, new StationMonitor());
        creature.tap();
        Permanent opponentCreature = addCreatureReady(player2, new StationMonitor());

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(altar), 1, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(opponentCreature.isTapped()).isFalse();
        assertThat(altar.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("Station can only be activated at sorcery speed")
    void stationRequiresSorcerySpeed() {
        Permanent altar = addAltarReady();
        addCreatureReady(player1, new StationMonitor());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(altar), 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    @Test
    @DisplayName("The draw ability uses modified power and can sacrifice a tapped creature")
    void sacrificeUsesModifiedPowerOfTappedCreature() {
        Permanent altar = addAltarReady();
        altar.setCounterCount(CounterType.CHARGE, 12);
        Permanent creature = addCreatureReady(player1, new StationMonitor());
        creature.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        creature.tap();
        harness.setLibrary(player1, List.of(new StationMonitor(), new StationMonitor(), new StationMonitor()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(altar), 2, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Station Monitor");
        harness.assertLife(player1, 18);
        assertThat(altar.getCounterCount(CounterType.CHARGE)).isEqualTo(12);
    }

    @Test
    @DisplayName("An activated draw ability still resolves after charge counters are removed")
    void drawAbilityDoesNotRecheckCountersAtResolution() {
        Permanent altar = addAltarReady();
        altar.setCounterCount(CounterType.CHARGE, 12);
        addCreatureReady(player1, new StationMonitor());
        harness.setLibrary(player1, List.of(new StationMonitor(), new StationMonitor(), new StationMonitor()));
        harness.setHand(player1, List.of());
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, battlefieldIndex(altar), 2, null, null);
        altar.setCounterCount(CounterType.CHARGE, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(altar.getCounterCount(CounterType.CHARGE)).isZero();
    }

    @Test
    @DisplayName("The draw ability cannot be activated without enough life to pay")
    void drawAbilityRequiresLifePayment() {
        Permanent altar = addAltarReady();
        altar.setCounterCount(CounterType.CHARGE, 12);
        Permanent creature = addCreatureReady(player1, new StationMonitor());
        harness.setLife(player1, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, battlefieldIndex(altar), 2, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("life");
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(creature);
        assertThat(altar.isTapped()).isFalse();
        harness.assertLife(player1, 1);
    }

    private Permanent addAltarReady() {
        return harness.addToBattlefieldAndReturn(player1, new SusurSecundiVoidAltar());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
