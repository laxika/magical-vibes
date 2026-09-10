package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SerrasBlessing.class, GrizzlyBears.class, Serenity.class})
class SerrasBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control gain vigilance")
    void ownCreaturesGainVigilance() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerrasBlessing());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Opponent creatures do not gain vigilance")
    void opponentCreaturesDoNotGainVigilance() {
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerrasBlessing());

        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Creatures entering under your control also gain vigilance")
    void creaturesEnteringLaterGainVigilance() {
        harness.addToBattlefield(player1, new SerrasBlessing());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Noncreature permanents do not gain vigilance")
    void noncreaturePermanentsDoNotGainVigilance() {
        Permanent serenity = harness.addToBattlefieldAndReturn(player1, new Serenity());
        harness.addToBattlefield(player1, new SerrasBlessing());

        assertThat(gqs.hasKeyword(gd, serenity, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Vigilance is removed when Serra's Blessing leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new SerrasBlessing());
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Serra's Blessing"));

        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }
}
