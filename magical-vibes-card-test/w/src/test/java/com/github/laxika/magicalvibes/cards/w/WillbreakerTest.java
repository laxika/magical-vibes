package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.cards.i.IcyManipulator;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Willbreaker.class, GiantGrowth.class, GrizzlyBears.class, Unsummon.class,
        IcyManipulator.class, TurnToFrog.class})
class WillbreakerTest extends BaseCardTest {

    @Test
    @DisplayName("An activated ability targeting an opponent's creature triggers Willbreaker")
    void gainsControlFromActivatedAbility() {
        harness.addToBattlefield(player1, new Willbreaker());
        harness.addToBattlefield(player1, new IcyManipulator());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 1, null, bears.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.passBothPriorities();
        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Willbreaker does not trigger after losing its abilities")
    void doesNotTriggerAfterLosingAbilities() {
        Permanent willbreaker = harness.addToBattlefieldAndReturn(player1, new Willbreaker());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new TurnToFrog()));
        harness.addMana(player2, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player2, 0, willbreaker.getId());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("No control is gained if Willbreaker leaves before its trigger resolves")
    void sourceLeavesBeforeTriggerResolves() {
        Permanent willbreaker = harness.addToBattlefieldAndReturn(player1, new Willbreaker());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player2, 0, willbreaker.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.controlEffectsFor(bears.getId())).isEmpty();
    }

    @Test
    @DisplayName("Gains control of an opponent creature targeted by your spell")
    void gainsControlOfTargetedOpponentCreature() {
        harness.addToBattlefield(player1, new Willbreaker());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Willbreaker");

        harness.passBothPriorities(); // resolve the Willbreaker trigger

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(bears.getId()));

        Permanent willbreaker = findPermanent(player1, "Willbreaker");
        assertThat(gd.newestControlEffectFor(bears.getId()).sourcePermanentId())
                .isEqualTo(willbreaker.getId());
    }

    @Test
    @DisplayName("Stolen creature returns when Willbreaker leaves the battlefield")
    void stolenCreatureReturnsWhenWillbreakerLeaves() {
        harness.addToBattlefield(player1, new Willbreaker());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());
        harness.passBothPriorities();

        Permanent willbreaker = findPermanent(player1, "Willbreaker");

        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.castInstant(player2, 0, willbreaker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getId().equals(bears.getId()));
        assertThat(gd.controlEffectsFor(bears.getId())).isEmpty();
    }

    @Test
    @DisplayName("Does not trigger when your spell targets your own creature")
    void doesNotTriggerOnYourOwnCreature() {
        harness.addToBattlefield(player1, new Willbreaker());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
    }

    @Test
    @DisplayName("Does not trigger when an opponent's spell targets their own creature")
    void doesNotTriggerOnOpponentSpell() {
        harness.addToBattlefield(player1, new Willbreaker());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new GiantGrowth()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castInstant(player2, 0, bears.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getCard().getName()).isEqualTo("Giant Growth");
    }
}
