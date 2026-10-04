package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.a.AccordersShield;
import com.github.laxika.magicalvibes.cards.o.OriginSpellbomb;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({GhalmasWarden.class, OriginSpellbomb.class, AccordersShield.class, Memnite.class})
class GhalmasWardenTest extends BaseCardTest {

    @Test
    @DisplayName("Base 2/4 with zero artifacts")
    void noMetalcraftWithZeroArtifacts() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new GhalmasWarden());

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(4);
    }

    @Test
    @DisplayName("Base 2/4 with two artifacts")
    void noMetalcraftWithTwoArtifacts() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new GhalmasWarden());
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addToBattlefield(player1, new AccordersShield());

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gets +2/+2 (becomes 4/6) with exactly three artifacts")
    void metalcraftWithThreeArtifacts() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new GhalmasWarden());
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addToBattlefield(player1, new AccordersShield());
        harness.addToBattlefield(player1, new Memnite());

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(6);
    }

    @Test
    @DisplayName("Loses boost when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new GhalmasWarden());
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addToBattlefield(player1, new AccordersShield());
        harness.addToBattlefield(player1, new Memnite());

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(4);

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Memnite"));
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(4);
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new GhalmasWarden());
        harness.addToBattlefield(player2, new OriginSpellbomb());
        harness.addToBattlefield(player2, new AccordersShield());
        harness.addToBattlefield(player2, new Memnite());

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(4);
    }

    @Test
    @DisplayName("Gains metalcraft immediately when a third artifact enters")
    void gainsMetalcraftWhenThirdArtifactEnters() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new GhalmasWarden());
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addToBattlefield(player1, new AccordersShield());
        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(4);

        harness.addToBattlefield(player1, new Memnite());

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(6);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Additional artifacts do not increase the bonus or boost another creature")
    void metalcraftBonusIsFixedAndOnlyAppliesToWarden() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new GhalmasWarden());
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addToBattlefield(player1, new AccordersShield());
        harness.addToBattlefield(player1, new Memnite());
        Permanent otherCreature = harness.addToBattlefieldAndReturn(player1, new Memnite());

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(6);
        assertThat(gqs.getEffectivePower(gd, otherCreature)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, otherCreature)).isEqualTo(1);
    }

    @Test
    @DisplayName("Own and opposing artifacts cannot be combined to reach metalcraft")
    void artifactsAreCountedSeparatelyForEachController() {
        Permanent warden = harness.addToBattlefieldAndReturn(player1, new GhalmasWarden());
        harness.addToBattlefield(player1, new OriginSpellbomb());
        harness.addToBattlefield(player1, new AccordersShield());
        harness.addToBattlefield(player2, new Memnite());

        assertThat(gqs.getEffectivePower(gd, warden)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, warden)).isEqualTo(4);
    }
}
