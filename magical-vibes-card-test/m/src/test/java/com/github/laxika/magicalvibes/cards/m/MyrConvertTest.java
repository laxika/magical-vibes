package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MyrConvert.class})
class MyrConvertTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping and paying 2 life adds one mana of the chosen color")
    void tapsForAnyColorMana() {
        Permanent myr = addCreatureReady(player1, new MyrConvert());
        int lifeBefore = gd.getLife(player1.getId());

        harness.activateAbility(player1, 0, null, null);

        assertThat(myr.isTapped()).isTrue();
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore - 2);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.ColorChoice.class);

        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot activate when the controller cannot pay 2 life")
    void cannotPayLifeCost() {
        addCreatureReady(player1, new MyrConvert());
        harness.setLife(player1, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @EnumSource(value = ManaColor.class, names = {"WHITE", "BLACK", "RED", "GREEN"})
    @DisplayName("The other four mana colors are also available")
    void tapsForOtherManaColors(ManaColor color) {
        Permanent myr = addCreatureReady(player1, new MyrConvert());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, color.name());

        assertThat(myr.isTapped()).isTrue();
        harness.assertLife(player1, 18);
        assertThat(gd.playerManaPools.get(player1.getId()).get(color)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Summoning sickness prevents tapping for mana without paying life")
    void summoningSicknessPreventsActivation() {
        harness.addToBattlefield(player1, new MyrConvert());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
        assertThat(findPermanent(player1, "Myr Convert").isTapped()).isFalse();
    }

    @Test
    @DisplayName("An already tapped Myr cannot activate or pay life again")
    void tappedMyrCannotActivate() {
        Permanent myr = addCreatureReady(player1, new MyrConvert());
        myr.tap();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Combat damage causes normal life loss and one poison counter immediately")
    void toxicAppliesAlongsideCombatDamage() {
        Permanent myr = addCreatureReady(player1, new MyrConvert());
        myr.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Paying life for mana does not give either player poison counters")
    void manaActivationDoesNotApplyToxic() {
        addCreatureReady(player1, new MyrConvert());

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        harness.assertLife(player1, 18);
        assertThat(gd.playerPoisonCounters.getOrDefault(player1.getId(), 0)).isZero();
        assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isZero();
    }
}
