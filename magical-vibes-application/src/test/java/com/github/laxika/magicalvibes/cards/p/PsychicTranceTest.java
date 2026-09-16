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

    private void castPsychicTrance() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castFromHand(player1, new PsychicTrance(), "{2}{U}{U}");
        harness.passBothPriorities();
    }
}
