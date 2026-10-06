package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Fungusaur;
import com.github.laxika.magicalvibes.cards.p.PhyrexianWalker;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SeismicWave.class, GrizzlyBears.class, PhyrexianWalker.class, Fungusaur.class})
class SeismicWaveTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to the any target and 1 damage to each nonartifact creature the opponent controls")
    void dealsBothDamagesAndSkipsArtifactCreatures() {
        harness.setLife(player2, 20);
        Permanent nonartifactCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent artifactCreature = addCreatureReady(player2, new PhyrexianWalker());
        harness.setHand(player1, List.of(new SeismicWave()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(player2.getId(), player2.getId()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(nonartifactCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(artifactCreature.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Requires the second target to be an opponent")
    void requiresOpponentTarget() {
        harness.setHand(player1, List.of(new SeismicWave()));
        harness.addMana(player1, ManaColor.RED, 3);

        assertThatThrownBy(() -> harness.castInstant(
                player1, 0, List.of(player1.getId(), player1.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can damage its controller while damaging only the opponent's nonartifact creatures")
    void canTargetControllerWithoutDamagingTheirCreatures() {
        harness.setLife(player1, 20);
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent secondOpposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeismicWave()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(player1.getId(), player2.getId()));

        harness.assertLife(player1, 18);
        assertThat(ownCreature.getMarkedDamage()).isZero();
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(secondOpposingCreature.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("An artifact creature can receive the targeted damage but skips the mass damage")
    void canTargetArtifactCreature() {
        Permanent artifactCreature = addCreatureReady(player2, new PhyrexianWalker());
        Permanent nonartifactCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeismicWave()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(artifactCreature.getId(), player2.getId()));

        assertThat(artifactCreature.getMarkedDamage()).isEqualTo(2);
        assertThat(nonartifactCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Phyrexian Walker");
    }

    @Test
    @DisplayName("A targeted nonartifact creature receives both portions of damage")
    void nonartifactTargetReceivesThreeDamage() {
        Permanent targetedCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeismicWave()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(targetedCreature.getId(), player2.getId()));

        assertThat(targetedCreature.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(targetedCreature);
        assertThat(otherCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Still damages the opponent's creatures when the first target leaves the battlefield")
    void resolvesMassDamageWhenFirstTargetIsGone() {
        Permanent targetedCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent remainingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new SeismicWave()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, List.of(targetedCreature.getId(), player2.getId()));

        gd.playerBattlefields.get(player2.getId()).remove(targetedCreature);
        harness.setGraveyard(player2, List.of(targetedCreature.getCard()));
        harness.passBothPriorities();

        assertThat(targetedCreature.getMarkedDamage()).isZero();
        assertThat(remainingCreature.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Seismic Wave");
    }

    @Test
    @DisplayName("Both damage portions trigger a targeted creature's dealt-damage ability only once")
    void bothDamagePortionsAreOneDamageEvent() {
        Permanent fungusaur = addCreatureReady(player2, new Fungusaur());
        fungusaur.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new SeismicWave()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castAndResolveInstant(player1, 0, List.of(fungusaur.getId(), player2.getId()));
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(fungusaur);
        assertThat(fungusaur.getMarkedDamage()).isEqualTo(3);
        assertThat(fungusaur.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }
}
