package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.w.Warthog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerrasBlessing.class, Warthog.class})
class SerrasBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain vigilance")
    void ownCreaturesGainVigilance() {
        Permanent warthog = addCreatureReady(player1, new Warthog());
        harness.addToBattlefield(player1, new SerrasBlessing());

        assertThat(gqs.hasKeyword(gd, warthog, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Creatures entering under your control also gain vigilance")
    void creaturesEnteringLaterGainVigilance() {
        harness.addToBattlefield(player1, new SerrasBlessing());
        Permanent warthog = addCreatureReady(player1, new Warthog());

        assertThat(gqs.hasKeyword(gd, warthog, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain vigilance")
    void opponentCreaturesDoNotGainVigilance() {
        Permanent opponentWarthog = addCreatureReady(player2, new Warthog());
        harness.addToBattlefield(player1, new SerrasBlessing());

        assertThat(gqs.hasKeyword(gd, opponentWarthog, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Vigilance is removed when Serra's Blessing leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent warthog = addCreatureReady(player1, new Warthog());
        Permanent blessing = harness.addToBattlefieldAndReturn(player1, new SerrasBlessing());
        assertThat(gqs.hasKeyword(gd, warthog, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId()).remove(blessing);

        assertThat(gqs.hasKeyword(gd, warthog, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Granted vigilance keeps an attacking creature untapped")
    void grantedVigilancePreventsAttackTap() {
        Permanent warthog = addCreatureReady(player1, new Warthog());
        harness.addToBattlefield(player1, new SerrasBlessing());

        declareAttackers(List.of(0));

        assertThat(warthog.isTapped()).isFalse();
    }
}
