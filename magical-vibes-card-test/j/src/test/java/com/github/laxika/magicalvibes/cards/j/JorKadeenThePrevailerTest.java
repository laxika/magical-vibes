package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.b.BottleGnomes;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.cards.s.Spellbook;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({JorKadeenThePrevailer.class, BottleGnomes.class, GrizzlyBears.class,
        LeoninScimitar.class, Spellbook.class})
class JorKadeenThePrevailerTest extends BaseCardTest {

    @Test
    @DisplayName("Base 5/4 with zero artifacts")
    void noMetalcraftWithZeroArtifacts() {
        Permanent jor = harness.addToBattlefieldAndReturn(player1, new JorKadeenThePrevailer());
        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(4);
    }

    @Test
    @DisplayName("No boost to other creatures without metalcraft")
    void noBoostToOtherCreaturesWithoutMetalcraft() {
        harness.addToBattlefield(player1, new JorKadeenThePrevailer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("No boost with only two artifacts")
    void noMetalcraftWithTwoArtifacts() {
        harness.addToBattlefield(player1, new JorKadeenThePrevailer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Own creatures get +3/+0 with three artifacts")
    void metalcraftBoostsOwnCreatures() {
        harness.addToBattlefield(player1, new JorKadeenThePrevailer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Jor Kadeen itself gets +3/+0 with metalcraft")
    void metalcraftBoostsSelf() {
        harness.addToBattlefield(player1, new JorKadeenThePrevailer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        Permanent jor = findPermanent(player1, "Jor Kadeen, the Prevailer");
        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, jor)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not boost opponent creatures")
    void doesNotBoostOpponentCreatures() {
        harness.addToBattlefield(player1, new JorKadeenThePrevailer());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent opponentBears = findPermanent(player2, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Loses boost when artifact count drops below three")
    void losesMetalcraftWhenArtifactRemoved() {
        harness.addToBattlefield(player1, new JorKadeenThePrevailer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);

        // Remove one artifact — now only 2
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Bottle Gnomes"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's artifacts don't count for metalcraft")
    void opponentArtifactsDontCount() {
        harness.addToBattlefield(player1, new JorKadeenThePrevailer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new Spellbook());
        harness.addToBattlefield(player2, new LeoninScimitar());
        harness.addToBattlefield(player2, new BottleGnomes());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Reaching three artifacts immediately boosts existing and newly entering creatures")
    void gainsMetalcraftWhenThirdArtifactEnters() {
        Permanent jor = harness.addToBattlefieldAndReturn(player1, new JorKadeenThePrevailer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);

        Permanent gnomes = harness.addToBattlefieldAndReturn(player1, new BottleGnomes());
        assertThat(gqs.getEffectivePower(gd, jor)).isEqualTo(8);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, gnomes)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, gnomes)).isEqualTo(3);

        Permanent newBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        assertThat(gqs.getEffectivePower(gd, newBears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, newBears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Boost ends when Jor Kadeen leaves even if metalcraft remains")
    void boostEndsWhenSourceLeaves() {
        Permanent jor = harness.addToBattlefieldAndReturn(player1, new JorKadeenThePrevailer());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new Spellbook());
        harness.addToBattlefield(player1, new LeoninScimitar());
        harness.addToBattlefield(player1, new BottleGnomes());
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);

        jor.setMarkedDamage(4);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Jor Kadeen, the Prevailer");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("First strike kills a blocker before it can deal damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent jor = addCreatureReady(player1, new JorKadeenThePrevailer());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Jor Kadeen, the Prevailer");
        assertThat(jor.getMarkedDamage()).isZero();
        harness.assertLife(player2, 20);
    }
}
