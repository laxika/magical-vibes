package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OgreSentryTest extends BaseCardTest {

    @Test
    @DisplayName("Ogre Sentry cannot attack because it has defender")
    void cannotAttackBecauseItHasDefender() {
        Permanent sentry = new Permanent(new OgreSentry());
        sentry.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sentry);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();

        assertThatThrownBy(() -> gs.declareAttackers(gd, player1, java.util.List.of(0)))
                .isInstanceOf(IllegalStateException.class);
    }
}
