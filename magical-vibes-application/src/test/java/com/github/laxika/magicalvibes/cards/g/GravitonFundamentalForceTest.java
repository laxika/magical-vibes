package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GravitonFundamentalForce.class, Forest.class, GrizzlyBears.class})
class GravitonFundamentalForceTest extends BaseCardTest {

    private static final String FLYING_MODE = "Target creature gains flying until end of turn";
    private static final String TAP_MODE = "Tap target creature";

    @Test
    @DisplayName("The second card draw can give a target creature flying until end of turn")
    void givesTargetCreatureFlying() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                new GrizzlyBears());
        harness.addToBattlefield(player1, new GravitonFundamentalForce());
        prepareLibrary();

        drawCard();
        drawCard();
        harness.passBothPriorities();
        harness.handleListChoice(player1, FLYING_MODE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, target, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("The second card draw can tap a target creature")
    void tapsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2,
                new GrizzlyBears());
        harness.addToBattlefield(player1, new GravitonFundamentalForce());
        prepareLibrary();

        drawCard();
        drawCard();
        harness.passBothPriorities();
        harness.handleListChoice(player1, TAP_MODE);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The modal trigger only allows creature targets")
    void rejectsNonCreatureTarget() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addToBattlefield(player1, new GravitonFundamentalForce());
        prepareLibrary();

        drawCard();
        drawCard();
        harness.passBothPriorities();
        harness.handleListChoice(player1, TAP_MODE);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void prepareLibrary() {
        harness.setLibrary(player1, List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
    }

    private void drawCard() {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
    }
}
