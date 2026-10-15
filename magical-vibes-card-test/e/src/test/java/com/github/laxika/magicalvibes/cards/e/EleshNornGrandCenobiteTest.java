package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MirrorGallery;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EleshNornGrandCenobite.class, GrizzlyBears.class, MirrorGallery.class})
class EleshNornGrandCenobiteTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new EleshNornGrandCenobite()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(EleshNornGrandCenobite.class);
    }

    @Test
    @DisplayName("Resolving puts Elesh Norn onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new EleshNornGrandCenobite()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Elesh Norn, Grand Cenobite");
    }

    @Test
    @DisplayName("Does not buff itself")
    void doesNotBuffItself() {
        Permanent norn = harness.addToBattlefieldAndReturn(player1, new EleshNornGrandCenobite());

        assertThat(gqs.getEffectivePower(gd, norn)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, norn)).isEqualTo(7);
    }

    @Test
    @DisplayName("Other own creatures get +2/+2")
    void buffsOwnCreatures() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Does not buff opponent creatures")
    void doesNotBuffOpponentCreatures() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        // Opponent bears get -2/-2 (not +2/+2): 2-2=0, 2-2=0
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(0);
    }

    @Test
    @DisplayName("Opponent creatures get -2/-2")
    void debuffsOpponentCreatures() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(0);
    }

    @Test
    @DisplayName("Does not debuff own creatures")
    void doesNotDebuffOwnCreatures() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        // Own bears get +2/+2 only: 2+2=4, 2+2=4
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Two Elesh Norns buff each other")
    void twoNornsBuffEachOther() {
        harness.addToBattlefield(player1, new MirrorGallery());
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.runStateBasedActions();

        List<Permanent> norns = findPermanents(player1, "Elesh Norn, Grand Cenobite");

        assertThat(norns).hasSize(2);
        for (Permanent norn : norns) {
            // Each gets +2/+2 from the other → 6/9
            assertThat(gqs.getEffectivePower(gd, norn)).isEqualTo(6);
            assertThat(gqs.getEffectiveToughness(gd, norn)).isEqualTo(9);
        }
    }

    @Test
    @DisplayName("Two Elesh Norns give +4/+4 to other own creatures")
    void twoNornsStackOwnBonus() {
        harness.addToBattlefield(player1, new MirrorGallery());
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.runStateBasedActions();

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        // 2/2 base + 4/4 from two Norns = 6/6
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(6);
    }

    @Test
    @DisplayName("Two Elesh Norns give -4/-4 to opponent creatures")
    void twoNornsStackOpponentPenalty() {
        harness.addToBattlefield(player1, new MirrorGallery());
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        // 2/2 base - 4/4 from two Norns = -2/-2
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(-2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(-2);

        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Own creature bonus is removed when Elesh Norn leaves")
    void ownBonusRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Elesh Norn, Grand Cenobite"));

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent creature penalty is removed when Elesh Norn leaves")
    void opponentPenaltyRemovedWhenSourceLeaves() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        Permanent opponentNorn = harness.addToBattlefieldAndReturn(player2, new EleshNornGrandCenobite());
        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, opponentNorn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentNorn)).isEqualTo(5);

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Elesh Norn, Grand Cenobite"));

        assertThat(gqs.getEffectivePower(gd, opponentNorn)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, opponentNorn)).isEqualTo(7);
    }

    @Test
    @DisplayName("Bonus applies when Elesh Norn resolves onto battlefield")
    void bonusAppliesOnResolve() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new EleshNornGrandCenobite()));
        harness.addMana(player1, ManaColor.WHITE, 7);

        Permanent ownBears = findPermanent(player1, "Grizzly Bears");
        // Before casting, no bonus
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, findPermanent(player2, "Grizzly Bears"))).isEqualTo(2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        // After resolving, own creature is buffed and the opposing creature dies.
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(4);
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Static bonus survives end-of-turn modifier reset")
    void staticBonusSurvivesEndOfTurnReset() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        // Add a temporary spell boost
        bears.setPowerModifier(bears.getPowerModifier() + 3);
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(7); // 2 base + 3 spell + 2 static

        // Reset end-of-turn modifiers
        bears.resetModifiers();

        // Spell bonus gone, static bonus still computed
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4); // 2 base + 2 static
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Elesh Norn attacks without tapping")
    void attacksWithoutTapping() {
        Permanent norn = addCreatureReady(player1, new EleshNornGrandCenobite());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(norn.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Opposing Elesh Norns each get only the opponent's penalty")
    void opposingNornsDebuffEachOtherAndCancelOtherCreatureBonuses() {
        Permanent ownNorn = harness.addToBattlefieldAndReturn(player1, new EleshNornGrandCenobite());
        Permanent opponentNorn = harness.addToBattlefieldAndReturn(player2, new EleshNornGrandCenobite());
        Permanent ownBears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentBears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.runStateBasedActions();

        assertThat(gqs.getEffectivePower(gd, ownNorn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownNorn)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, opponentNorn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentNorn)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, opponentBears)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Creatures entering after Elesh Norn immediately receive her bonus")
    void laterFriendlyCreatureReceivesBonus() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("An opposing creature entering after Elesh Norn dies from zero toughness")
    void laterOpposingCreatureDiesImmediately() {
        harness.addToBattlefield(player1, new EleshNornGrandCenobite());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.forceActivePlayer(player2);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(gd.stack).isEmpty();
    }
}
