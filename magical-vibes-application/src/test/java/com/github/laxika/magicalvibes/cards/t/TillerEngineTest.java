package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DismalBackwater;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TillerEngine.class, DismalBackwater.class, Forest.class, GrizzlyBears.class})
class TillerEngineTest extends BaseCardTest {

    private static final String UNTAP = "Untap that land.";
    private static final String TAP = "Tap target nonland permanent an opponent controls.";

    @Test
    void untapsTheTappedLand() {
        harness.addToBattlefield(player1, new TillerEngine());

        playTappedLand();
        Permanent land = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof DismalBackwater)
                .findFirst()
                .orElseThrow();
        assertThat(land.isTapped()).isTrue();

        harness.handleListChoice(player1, UNTAP);
        harness.passBothPriorities();

        assertThat(land.isTapped()).isFalse();
    }

    @Test
    void tapsTargetNonlandPermanentAnOpponentControls() {
        harness.addToBattlefield(player1, new TillerEngine());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        playTappedLand();
        harness.handleListChoice(player1, TAP);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    void tapModeCannotTargetYourPermanent() {
        harness.addToBattlefield(player1, new TillerEngine());
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        playTappedLand();
        harness.handleListChoice(player1, TAP);

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void untappedLandDoesNotTrigger() {
        harness.addToBattlefield(player1, new TillerEngine());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private void playTappedLand() {
        harness.setHand(player1, List.of(new DismalBackwater()));
        harness.playLand(player1, 0);
    }
}
