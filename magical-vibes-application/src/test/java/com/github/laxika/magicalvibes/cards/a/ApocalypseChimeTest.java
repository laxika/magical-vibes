package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CemeteryGate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MesaFalcon;
import com.github.laxika.magicalvibes.cards.s.SpectralBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ApocalypseChimeTest extends BaseCardTest {

    /** Chime on player1's battlefield with two mana available in their main phase. */
    private void chimeReady() {
        harness.addToBattlefield(player1, new ApocalypseChime());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void ringChime() {
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Destroys Homelands permanents on both battlefields and spares everything else")
    void destroysOnlyHomelandsNames() {
        chimeReady();
        harness.addToBattlefield(player1, new SpectralBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new MesaFalcon());
        harness.addToBattlefield(player2, new Forest());

        ringChime();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Forest");
        harness.assertInGraveyard(player1, "Spectral Bears");
        harness.assertInGraveyard(player2, "Mesa Falcon");
    }

    @Test
    @DisplayName("The Chime itself is sacrificed as a cost, so it is gone even before resolution")
    void chimeIsSacrificedAsACost() {
        chimeReady();

        harness.activateAbility(player1, 0, null, null);

        harness.assertInGraveyard(player1, "Apocalypse Chime");
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("A regeneration shield does not save a destroyed Homelands permanent")
    void regenerationCannotSaveThem() {
        chimeReady();
        Permanent gate = harness.addToBattlefieldAndReturn(player2, new CemeteryGate());
        gate.setRegenerationShield(1);

        ringChime();

        assertThat(gd.playerBattlefields.get(player2.getId())).isEmpty();
        harness.assertInGraveyard(player2, "Cemetery Gate");
    }

    @Test
    @DisplayName("A token with a Homelands name survives")
    void tokenWithHomelandsNameSurvives() {
        chimeReady();
        Card token = new SpectralBears();
        token.setToken(true);
        harness.addToBattlefield(player2, token);

        ringChime();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Spectral Bears");
    }
}
