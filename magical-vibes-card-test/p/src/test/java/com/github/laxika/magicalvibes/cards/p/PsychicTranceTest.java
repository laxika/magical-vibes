package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.cards.n.NamelessOne;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsychicTrance.class, NamelessOne.class, GlorySeeker.class})
class PsychicTranceTest extends BaseCardTest {

    @Test
    @DisplayName("Wizards you control can tap to counter a spell")
    void wizardsCanCounterSpell() {
        Permanent wizard = addCreatureReady(player1, new NamelessOne());
        addCreatureReady(player1, new GlorySeeker());
        castPsychicTrance();

        PsychicTrance spell = new PsychicTrance();
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, spell, "{2}{U}{U}");
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, spell.getId());
        harness.passBothPriorities();

        assertThat(wizard.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Psychic Trance");
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, spell.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Only Wizards you control gain the counter ability")
    void onlyControlledWizardsGainAbility() {
        addCreatureReady(player1, new NamelessOne());
        addCreatureReady(player2, new NamelessOne());
        castPsychicTrance();

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("The granted counter ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        addCreatureReady(player1, new NamelessOne());
        castPsychicTrance();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Wizards entering after resolution do not gain the ability")
    void laterWizardsDoNotGainAbility() {
        addCreatureReady(player1, new NamelessOne());
        castPsychicTrance();
        addCreatureReady(player1, new NamelessOne());

        PsychicTrance spell = new PsychicTrance();
        harness.castFromHand(player1, spell, "{2}{U}{U}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, spell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no activated ability");
    }

    @Test
    @DisplayName("A summoning-sick Wizard cannot pay the granted tap cost")
    void summoningSickWizardCannotActivate() {
        Permanent wizard = addCreatureReady(player1, new NamelessOne());
        wizard.setSummoningSick(true);
        castPsychicTrance();

        PsychicTrance spell = new PsychicTrance();
        harness.castFromHand(player1, spell, "{2}{U}{U}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
        assertThat(wizard.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A tapped Wizard cannot pay the granted tap cost")
    void tappedWizardCannotActivate() {
        Permanent wizard = addCreatureReady(player1, new NamelessOne());
        wizard.tap();
        castPsychicTrance();

        PsychicTrance spell = new PsychicTrance();
        harness.castFromHand(player1, spell, "{2}{U}{U}");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, spell.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    @Test
    @DisplayName("A Wizard can counter its controller's spell")
    void canCounterOwnSpell() {
        Permanent wizard = addCreatureReady(player1, new NamelessOne());
        castPsychicTrance();

        GlorySeeker spell = new GlorySeeker();
        harness.castFromHand(player1, spell, "{1}{W}");
        harness.activateAbility(player1, 0, null, spell.getId());
        harness.passBothPriorities();

        assertThat(wizard.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Glory Seeker");
        harness.assertNotOnBattlefield(player1, "Glory Seeker");
    }

    private void castPsychicTrance() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new PsychicTrance(), "{2}{U}{U}");
        harness.passBothPriorities();
    }
}
