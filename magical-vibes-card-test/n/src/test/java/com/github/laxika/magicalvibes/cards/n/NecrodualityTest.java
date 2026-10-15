package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RobeOfMirrors;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Necroduality.class, ScatheZombies.class, GrizzlyBears.class, Unsummon.class, RobeOfMirrors.class})
class NecrodualityTest extends BaseCardTest {

    @Test
    @DisplayName("Nontoken Zombie entering creates a token copy")
    void nontokenZombieEnteringCreatesTokenCopy() {
        harness.addToBattlefield(player1, new Necroduality());
        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");
        harness.passBothPriorities(); // resolve creature spell; Necroduality triggers
        harness.passBothPriorities(); // resolve copy trigger

        long zombies = countPermanents(player1, "Scathe Zombies");
        assertThat(zombies).isEqualTo(2);

        long tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Scathe Zombies") && p.getCard().isToken())
                .count();
        assertThat(tokens).isEqualTo(1);
    }

    @Test
    @DisplayName("Token copy does not retrigger Necroduality")
    void tokenCopyDoesNotRetrigger() {
        harness.addToBattlefield(player1, new Necroduality());
        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Only one token; the copy's ETB must not fire Necroduality again.
        long tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Scathe Zombies") && p.getCard().isToken())
                .count();
        assertThat(tokens).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("Non-Zombie creature entering does not trigger")
    void nonZombieDoesNotTrigger() {
        harness.addToBattlefield(player1, new Necroduality());
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        long bears = countPermanents(player1, "Grizzly Bears");
        assertThat(bears).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Opponent's nontoken Zombie entering does not trigger")
    void opponentZombieDoesNotTrigger() {
        harness.addToBattlefield(player1, new Necroduality());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.castFromHand(player2, new ScatheZombies(), "{2}{B}");
        harness.passBothPriorities();

        long zombies = countPermanents(player2, "Scathe Zombies");
        assertThat(zombies).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Copy is created even if the entering Zombie leaves before resolution")
    void copiesZombieAfterItLeavesBattlefield() {
        harness.addToBattlefield(player1, new Necroduality());
        harness.setHand(player1, List.of(new ScatheZombies(), new Unsummon()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.castAndResolveInstant(player1, 0, harness.getPermanentId(player1, "Scathe Zombies"));
        harness.assertNotOnBattlefield(player1, "Scathe Zombies");
        harness.assertInHand(player1, "Scathe Zombies");
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anySatisfy(p -> {
                    assertThat(p.getCard().getName()).isEqualTo("Scathe Zombies");
                    assertThat(p.getCard().isToken()).isTrue();
                });
        assertThat(countPermanents(player1, "Scathe Zombies")).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Each Necroduality creates one copy without token recursion")
    void multipleNecrodualitiesCreateSeparateCopies() {
        harness.addToBattlefield(player1, new Necroduality());
        harness.addToBattlefield(player1, new Necroduality());
        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Scathe Zombies")).isEqualTo(3);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Scathe Zombies") && p.getCard().isToken())
                .count()).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Shroud does not prevent the nontargeting copy ability")
    void copiesZombieWithShroud() {
        harness.addToBattlefield(player1, new Necroduality());
        harness.castFromHand(player1, new ScatheZombies(), "{2}{B}");
        harness.passBothPriorities();
        var robe = harness.addToBattlefieldAndReturn(player1, new RobeOfMirrors());
        robe.setAttachedTo(harness.getPermanentId(player1, "Scathe Zombies"));

        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Scathe Zombies")).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }
}
