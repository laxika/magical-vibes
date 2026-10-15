package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MariposaMilitaryBase.class, Forest.class})
class MariposaMilitaryBaseTest extends BaseCardTest {

    @Test
    @DisplayName("May enter tapped and give its controller two rad counters")
    void mayEnterTappedForRadCounters() {
        harness.setHand(player1, List.of(new MariposaMilitaryBase()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);

        Permanent base = findPermanent(player1, "Mariposa Military Base");
        assertThat(base.isTapped()).isTrue();
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Declining to enter tapped leaves the land untapped without rad counters")
    void mayEnterUntappedWithoutRadCounters() {
        harness.setHand(player1, List.of(new MariposaMilitaryBase()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, false);

        Permanent base = findPermanent(player1, "Mariposa Military Base");
        assertThat(base.isTapped()).isFalse();
        assertThat(gd.playerRadCounters.get(player1.getId())).isNull();
    }

    @Test
    @DisplayName("Adds colorless mana")
    void addsColorlessMana() {
        Permanent base = addReadyBase(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isEqualTo(1);
        assertThat(base.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Rad counters reduce the draw ability activation cost")
    void radCountersReduceDrawAbilityCost() {
        addReadyBase(player1);
        gd.playerRadCounters.put(player1.getId(), 3);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("An opponent's rad counters do not reduce the draw ability activation cost")
    void opponentRadCountersDoNotReduceCost() {
        addReadyBase(player1);
        gd.playerRadCounters.put(player2.getId(), 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addReadyBase(Player player) {
        Permanent base = harness.addToBattlefieldAndReturn(player, new MariposaMilitaryBase());
        base.setSummoningSick(false);
        return base;
    }

    @Test
    @DisplayName("Without rad counters the draw ability costs five mana and taps the land")
    void drawWithoutRadCountersCostsFiveMana() {
        Permanent base = addReadyBase(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(base.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertNotInHand(player1, "Forest");

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @ParameterizedTest
    @ValueSource(ints = {5, 7})
    @DisplayName("Five or more rad counters reduce the mana cost to zero but still require tapping")
    void drawWithEnoughRadCountersCostsNoMana(int counters) {
        Permanent base = addReadyBase(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.playerRadCounters.put(player1.getId(), counters);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(base.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(counters);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Forest");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Changing rad counters after activation does not change the paid cost or draw")
    void radCountersAreCountedWhenAbilityIsActivated() {
        addReadyBase(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        gd.playerRadCounters.put(player1.getId(), 3);
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        gd.playerRadCounters.remove(player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Choosing tapped entry adds two rad counters to existing counters")
    void tappedEntryAddsToExistingRadCounters() {
        gd.playerRadCounters.put(player1.getId(), 3);
        harness.setHand(player1, List.of(new MariposaMilitaryBase()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.playLand(player1, 0);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(findPermanent(player1, "Mariposa Military Base").isTapped()).isTrue();
        assertThat(gd.playerRadCounters.get(player1.getId())).isEqualTo(5);
        assertThat(gd.playerRadCounters.get(player2.getId())).isNull();
    }
}
