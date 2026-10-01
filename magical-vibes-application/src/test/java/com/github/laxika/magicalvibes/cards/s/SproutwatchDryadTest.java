package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FugitiveWizard;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SproutwatchDryad.class, FugitiveWizard.class, GrizzlyBears.class})
class SproutwatchDryadTest extends BaseCardTest {

    private void advanceToCombatAndResolve(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Gains a keyword shared by a creature you control")
    void gainsKeywordSharedByControlledCreature() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new SproutwatchDryad());
        Permanent wizard = harness.addToBattlefieldAndReturn(player1, new FugitiveWizard());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        advanceToCombatAndResolve(player1);

        assertThat(gqs.hasKeyword(gd, dryad, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, wizard, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Gains a keyword found on a card in its controller's hand")
    void gainsKeywordFoundInHand() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new SproutwatchDryad());
        harness.setHand(player1, List.of(new FugitiveWizard()));

        advanceToCombatAndResolve(player1);

        assertThat(gqs.hasKeyword(gd, dryad, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Does not gain keywords without a matching creature or hand card")
    void doesNothingWithoutMatchingKeyword() {
        Permanent dryad = harness.addToBattlefieldAndReturn(player1, new SproutwatchDryad());
        harness.setHand(player1, List.of(new GrizzlyBears()));

        advanceToCombatAndResolve(player1);

        assertThat(gqs.hasKeyword(gd, dryad, Keyword.FLYING)).isFalse();
    }
}
