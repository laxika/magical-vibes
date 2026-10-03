package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.FarbogExplorer;
import com.github.laxika.magicalvibes.cards.o.Opalescence;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BuildersBlessing.class, FarbogExplorer.class})
class BuildersBlessingTest extends BaseCardTest {

    @Test
    @DisplayName("Untapped creature you control gets +0/+2")
    void untappedOwnCreatureGetsBoost() {
        harness.addToBattlefield(player1, new BuildersBlessing());
        Permanent explorer = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());

        assertThat(gqs.getEffectivePower(gd, explorer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, explorer)).isEqualTo(5);
    }

    @Test
    @DisplayName("Boost is lost while tapped and restored when it untaps")
    void boostFollowsTapState() {
        harness.addToBattlefield(player1, new BuildersBlessing());
        Permanent explorer = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());

        explorer.tap();
        assertThat(gqs.getEffectiveToughness(gd, explorer)).isEqualTo(3);

        explorer.untap();
        assertThat(gqs.getEffectiveToughness(gd, explorer)).isEqualTo(5);
    }

    @Test
    @DisplayName("Does not buff opponent's untapped creatures")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new BuildersBlessing());
        Permanent opponentExplorer = harness.addToBattlefieldAndReturn(player2, new FarbogExplorer());

        assertThat(gqs.getEffectivePower(gd, opponentExplorer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentExplorer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Bonus is removed when Builder's Blessing leaves the battlefield")
    void bonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new BuildersBlessing());
        Permanent explorer = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());
        assertThat(gqs.getEffectiveToughness(gd, explorer)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Builder's Blessing"));

        assertThat(gqs.getEffectiveToughness(gd, explorer)).isEqualTo(3);
    }

    @Test
    @DisplayName("Multiple blessings stack only while the creature is untapped")
    void multipleBlessingsStack() {
        harness.addToBattlefield(player1, new BuildersBlessing());
        harness.addToBattlefield(player1, new BuildersBlessing());
        Permanent explorer = harness.addToBattlefieldAndReturn(player1, new FarbogExplorer());

        assertThat(gqs.getEffectivePower(gd, explorer)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, explorer)).isEqualTo(7);

        explorer.tap();
        assertThat(gqs.getEffectiveToughness(gd, explorer)).isEqualTo(3);

        explorer.untap();
        assertThat(gqs.getEffectiveToughness(gd, explorer)).isEqualTo(7);
    }

    @Test
    @CardUsed({Opalescence.class})
    @DisplayName("An animated Builder's Blessing receives its own bonus while untapped")
    void animatedBlessingReceivesItsOwnBonus() {
        harness.addToBattlefield(player1, new Opalescence());
        Permanent blessing = harness.addToBattlefieldAndReturn(player1, new BuildersBlessing());

        assertThat(gqs.getEffectivePower(gd, blessing)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, blessing)).isEqualTo(6);

        blessing.tap();
        assertThat(gqs.getEffectiveToughness(gd, blessing)).isEqualTo(4);

        blessing.untap();
        assertThat(gqs.getEffectiveToughness(gd, blessing)).isEqualTo(6);
    }
}
