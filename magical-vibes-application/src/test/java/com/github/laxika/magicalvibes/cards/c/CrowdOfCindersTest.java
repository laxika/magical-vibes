package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Deathgazer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.o.Ornithopter;
import com.github.laxika.magicalvibes.cards.p.PhyrexianArena;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrowdOfCinders.class, Deathgazer.class, GrizzlyBears.class, Ornithopter.class,
        PhyrexianArena.class, ScatheZombies.class})
class CrowdOfCindersTest extends BaseCardTest {

    @Test
    @DisplayName("Counts itself as a black permanent when alone: 1/1")
    void countsItselfWhenAlone() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());

        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T equals the number of black permanents you control")
    void ptEqualsBlackPermanents() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        harness.addToBattlefield(player1, new Deathgazer());
        harness.addToBattlefield(player1, new Deathgazer());

        // itself + 2 black creatures = 3
        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(3);
    }

    @Test
    @DisplayName("Non-black permanents are not counted")
    void nonBlackNotCounted() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        harness.addToBattlefield(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(1);
    }

    @Test
    @DisplayName("Only counts your black permanents, not the opponent's")
    void countsOnlyControllersPermanents() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        harness.addToBattlefield(player2, new Deathgazer());

        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T updates when black permanents change")
    void ptUpdatesWhenBlackPermanentsChange() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        harness.addToBattlefield(player1, new Deathgazer());
        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(2);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Deathgazer"));
        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counts black noncreature permanents")
    void countsBlackNoncreaturePermanents() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        harness.addToBattlefield(player1, new PhyrexianArena());

        assertThat(gqs.getEffectivePower(gd, crowd)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, crowd)).isEqualTo(2);
    }

    @Test
    @DisplayName("Fear prevents nonblack nonartifact creatures from blocking")
    void fearPreventsNonblackNonartifactBlockers() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(
                        gd.playerBattlefields.get(player2.getId()).indexOf(bears),
                        gd.playerBattlefields.get(player1.getId()).indexOf(crowd)))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    @DisplayName("Fear allows black and artifact creatures to block")
    void fearAllowsBlackAndArtifactBlockers() {
        Permanent crowd = addCreatureReady(player1, new CrowdOfCinders());
        Permanent blackBlocker = addCreatureReady(player2, new ScatheZombies());
        Permanent artifactBlocker = addCreatureReady(player2, new Ornithopter());

        declareAttackersAndPrepareBlockers(List.of(0));
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(crowd);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(blackBlocker),
                        attackerIndex),
                new BlockerAssignment(gd.playerBattlefields.get(player2.getId()).indexOf(artifactBlocker),
                        attackerIndex)));

        assertThat(blackBlocker.isBlocking()).isTrue();
        assertThat(artifactBlocker.isBlocking()).isTrue();
    }
}
