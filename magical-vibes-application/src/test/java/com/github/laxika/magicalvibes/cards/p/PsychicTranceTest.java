package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Opt;
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

@CardUsed({PsychicTrance.class, FugitiveWizard.class, GrizzlyBears.class, Opt.class})
class PsychicTranceTest extends BaseCardTest {

    @Test
    @DisplayName("Wizards you control can tap to counter a spell")
    void wizardsCanCounterSpell() {
        Permanent wizard = addCreatureReady(player1, new FugitiveWizard());
        addCreatureReady(player1, new GrizzlyBears());
        castPsychicTrance();

        Opt opt = new Opt();
        harness.setHand(player2, List.of(opt));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.forceActivePlayer(player2);
        harness.castInstant(player2, 0);
        harness.passPriority(player2);

        harness.activateAbility(player1, 0, null, opt.getId());
        harness.passBothPriorities();

        assertThat(wizard.isTapped()).isTrue();
        harness.assertInGraveyard(player2, "Opt");
        assertThatThrownBy(() -> harness.activateAbility(player1, 1, null, opt.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("The granted counter ability expires at end of turn")
    void grantedAbilityExpiresAtEndOfTurn() {
        addCreatureReady(player1, new FugitiveWizard());
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
        harness.setHand(player1, List.of(new PsychicTrance()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
