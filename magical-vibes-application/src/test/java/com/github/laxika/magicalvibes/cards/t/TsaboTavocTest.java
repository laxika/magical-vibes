package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.c.CaptainSisay;
import com.github.laxika.magicalvibes.cards.k.KavuTitan;
import com.github.laxika.magicalvibes.cards.k.KeldonNecropolis;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TsaboTavoc.class, CaptainSisay.class, KavuTitan.class, KeldonNecropolis.class})
class TsaboTavocTest extends BaseCardTest {

    @Test
    @DisplayName("Activating Tsabo Tavoc destroys a target legendary creature")
    void destroysTargetLegendaryCreature() {
        Permanent tsaboTavoc = addTsaboTavoc();
        Permanent captainSisay = harness.addToBattlefieldAndReturn(player2, new CaptainSisay());
        captainSisay.setRegenerationShield(1);
        addBlackMana();

        harness.activateAbility(player1, 0, null, captainSisay.getId());
        assertThat(tsaboTavoc.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Captain Sisay");
        harness.assertInGraveyard(player2, "Captain Sisay");
    }

    @Test
    @DisplayName("Cannot target a nonlegendary creature")
    void cannotTargetNonlegendaryCreature() {
        addTsaboTavoc();
        Permanent kavuTitan = harness.addToBattlefieldAndReturn(player2, new KavuTitan());
        addBlackMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, kavuTitan.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    @Test
    @DisplayName("Cannot target a legendary noncreature permanent")
    void cannotTargetLegendaryNoncreaturePermanent() {
        addTsaboTavoc();
        Permanent keldonNecropolis = harness.addToBattlefieldAndReturn(player2, new KeldonNecropolis());
        addBlackMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, keldonNecropolis.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("legendary creature");
    }

    private void addBlackMana() {
        harness.addMana(player1, ManaColor.BLACK, 2);
    }

    private Permanent addTsaboTavoc() {
        return addCreatureReady(player1, new TsaboTavoc());
    }
}
