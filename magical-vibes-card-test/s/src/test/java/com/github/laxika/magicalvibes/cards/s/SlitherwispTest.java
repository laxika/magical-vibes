package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AngelsMercy;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.h.HussarPatrol;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.StackEntryType;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Slitherwisp.class, AngelsMercy.class, Forest.class, HussarPatrol.class})
class SlitherwispTest extends BaseCardTest {

    @Test
    @DisplayName("Casting another spell with flash draws a card and makes each opponent lose 1 life")
    void flashSpellTriggersDrawAndLifeLoss() {
        harness.addToBattlefield(player1, new Slitherwisp());
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new HussarPatrol(), "{2}{W}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getLife(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Casting a spell without flash does not trigger Slitherwisp")
    void nonFlashSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Slitherwisp());
        harness.castFromHand(player1, new AngelsMercy(), "{2}{W}{W}");

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
    }

    @Test
    @DisplayName("Slitherwisp does not trigger for its own cast")
    void ownCastDoesNotTrigger() {
        harness.castFromHand(player1, new Slitherwisp(), "{U}{B}{B}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Slitherwisp");
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An opponent's spell with flash does not trigger Slitherwisp")
    void opponentsFlashSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Slitherwisp());

        harness.castFromHand(player2, new Slitherwisp(), "{U}{B}{B}");

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Slitherwisp");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Another Slitherwisp triggers each existing Slitherwisp before the spell resolves")
    void eachExistingSlitherwispTriggersIndependently() {
        harness.addToBattlefield(player1, new Slitherwisp());
        harness.addToBattlefield(player1, new Slitherwisp());
        harness.setLibrary(player1, List.of(new Forest(), new Forest()));

        harness.castFromHand(player1, new Slitherwisp(), "{U}{B}{B}");

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 19);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player2, 18);

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        harness.assertLife(player2, 18);
    }
}
