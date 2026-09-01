package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.CemeteryGate;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MesaFalcon;
import com.github.laxika.magicalvibes.cards.s.SpectralBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ApocalypseChime.class, AetherStorm.class, CemeteryGate.class, Forest.class,
        GrizzlyBears.class, MesaFalcon.class, SpectralBears.class})
class ApocalypseChimeTest extends BaseCardTest {

    /** Chime on player1's battlefield with two mana available in their main phase. */
    private void chimeReady() {
        prepareChime(player1, TurnStep.PRECOMBAT_MAIN, 2);
    }

    private void prepareChime(Player activePlayer, TurnStep step, int mana) {
        harness.addToBattlefield(player1, new ApocalypseChime());
        harness.addMana(player1, ManaColor.COLORLESS, mana);
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(step);
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
        harness.addToBattlefield(player2, new AetherStorm());

        ringChime();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .extracting(permanent -> permanent.getCard().getName())
                .containsExactly("Forest");
        harness.assertInGraveyard(player1, "Spectral Bears");
        harness.assertInGraveyard(player2, "Mesa Falcon");
        harness.assertInGraveyard(player2, "Aether Storm");
    }

    @Test
    @DisplayName("The ability can be activated outside the main phase")
    void canActivateOutsideMainPhase() {
        prepareChime(player1, TurnStep.END_STEP, 2);
        harness.addToBattlefield(player2, new SpectralBears());

        ringChime();

        harness.assertInGraveyard(player2, "Spectral Bears");
    }

    @Test
    @DisplayName("The ability cannot be activated without its two-mana cost")
    void cannotActivateWithoutEnoughMana() {
        prepareChime(player1, TurnStep.PRECOMBAT_MAIN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        harness.assertOnBattlefield(player1, "Apocalypse Chime");
        harness.assertNotInGraveyard(player1, "Apocalypse Chime");
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

    @Test
    @DisplayName("A permanent with an HML spell name is also destroyed")
    void destroysPermanentWithHomelandsSpellName() {
        chimeReady();
        Card renamedPermanent = new GrizzlyBears();
        renamedPermanent.setName("An-Havva Inn");
        harness.addToBattlefield(player2, renamedPermanent);

        ringChime();

        harness.assertInGraveyard(player2, "An-Havva Inn");
    }
}
