package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.FortifyingDraught;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({AccomplishedAlchemist.class, FortifyingDraught.class})
class AccomplishedAlchemistTest extends BaseCardTest {

    @Test
    @DisplayName("First ability adds one mana of the chosen color")
    void firstAbilityAddsOneMana() {
        Permanent alchemist = addReadyAlchemist();

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(alchemist.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.RED.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Second ability adds mana equal to life gained this turn")
    void secondAbilityAddsManaEqualToLifeGained() {
        Permanent alchemist = addReadyAlchemist();
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(alchemist.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, ManaColor.GREEN.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(4);
    }

    @Test
    @DisplayName("Second ability counts only life gained by its controller")
    void secondAbilityIgnoresOpponentsLifeGained() {
        addReadyAlchemist();
        gd.lifeGainedThisTurn.put(player2.getId(), 4);

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Second ability accumulates multiple life gains and produces all mana in one color immediately")
    void secondAbilityCountsMultipleLifeGains() {
        Permanent alchemist = addReadyAlchemist();
        harness.setHand(player1, List.of(new FortifyingDraught(), new FortifyingDraught()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castAndResolveInstant(player1, 0, alchemist.getId());
        harness.castAndResolveInstant(player1, 0, alchemist.getId());

        harness.activateAbility(player1, 0, 1, null, null);
        assertThat(gd.stack).isEmpty();
        harness.handleListChoice(player1, ManaColor.WHITE.name());

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isEqualTo(4);
        for (ManaColor color : ManaColor.values()) {
            if (color != ManaColor.WHITE) {
                assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
            }
        }
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(alchemist.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability still taps when no life has been gained")
    void secondAbilityWithZeroLifeGained() {
        Permanent alchemist = addReadyAlchemist();

        harness.activateAbility(player1, 0, 1, null, null);

        assertThat(alchemist.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        for (ManaColor color : ManaColor.values()) {
            assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isZero();
        }
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both tap abilities are blocked by summoning sickness")
    void cannotActivateWhileSummoningSick(int abilityIndex) {
        harness.addToBattlefield(player1, new AccomplishedAlchemist());
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1})
    @DisplayName("Both tap abilities are blocked while tapped")
    void cannotActivateWhileTapped(int abilityIndex) {
        Permanent alchemist = addReadyAlchemist();
        alchemist.tap();
        gd.lifeGainedThisTurn.put(player1.getId(), 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, abilityIndex, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyAlchemist() {
        return addCreatureReady(player1, new AccomplishedAlchemist());
    }
}
