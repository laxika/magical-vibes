package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.a.AbyssalSpecter;
import com.github.laxika.magicalvibes.cards.b.BalduvianBarbarians;
import com.github.laxika.magicalvibes.cards.y.YavimayaGnats;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({OrcishHealer.class, AbyssalSpecter.class, BalduvianBarbarians.class, YavimayaGnats.class})
class OrcishHealerTest extends BaseCardTest {

    @Test
    @DisplayName("{R}{R}, {T} marks target creature so it can't be regenerated this turn")
    void preventRegeneration() {
        Permanent healer = addCreatureReady(player1, new OrcishHealer());
        Permanent specter = addCreatureReady(player2, new AbyssalSpecter());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, specter.getId());
        harness.passBothPriorities();

        assertThat(healer.isTapped()).isTrue();
        assertThat(specter.isCantRegenerateThisTurn()).isTrue();
    }

    @Test
    @DisplayName("The prevention ability can target a red creature")
    void preventRegenerationCanTargetRedCreature() {
        addCreatureReady(player1, new OrcishHealer());
        Permanent barbarians = addCreatureReady(player2, new BalduvianBarbarians());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, 0, null, barbarians.getId());
        harness.passBothPriorities();

        assertThat(barbarians.isCantRegenerateThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Prevention stops an existing regeneration shield from saving a creature")
    void preventionStopsRegenerationShield() {
        addCreatureReady(player1, new OrcishHealer());
        Permanent gnats = addCreatureReady(player1, new YavimayaGnats());

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.RED, 2);
        harness.activateAbility(player1, 0, 0, null, gnats.getId());
        harness.passBothPriorities();

        assertThat(gnats.getRegenerationShield()).isEqualTo(1);
        assertThat(gnats.isCantRegenerateThisTurn()).isTrue();

        gnats.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "Yavimaya Gnats");
        harness.assertInGraveyard(player1, "Yavimaya Gnats");
    }

    @Test
    @DisplayName("{B}{B}{R}, {T} regenerates a black creature")
    void blackModeRegeneratesBlackCreature() {
        addCreatureReady(player1, new OrcishHealer());
        Permanent specter = addCreatureReady(player1, new AbyssalSpecter());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 1, null, specter.getId());
        harness.passBothPriorities();

        assertThat(specter.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("{R}{G}{G}, {T} regenerates a green creature")
    void greenModeRegeneratesGreenCreature() {
        addCreatureReady(player1, new OrcishHealer());
        Permanent gnats = addCreatureReady(player1, new YavimayaGnats());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, 0, 2, null, gnats.getId());
        harness.passBothPriorities();

        assertThat(gnats.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration abilities cannot target a red creature")
    void cannotRegenerateRedCreature() {
        addCreatureReady(player1, new OrcishHealer());
        Permanent barbarians = addCreatureReady(player1, new BalduvianBarbarians());
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, barbarians.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black or green");
    }

    @Test
    @DisplayName("Orcish Healer cannot regenerate itself — it is red")
    void cannotRegenerateItself() {
        Permanent healer = addCreatureReady(player1, new OrcishHealer());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, null, healer.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("black or green");
    }
}
