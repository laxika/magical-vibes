package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HierophantsChalice;
import com.github.laxika.magicalvibes.cards.l.LegionsLanding;
import com.github.laxika.magicalvibes.cards.m.MarchOfTheMachines;
import com.github.laxika.magicalvibes.cards.p.PriestOfUrabrask;
import com.github.laxika.magicalvibes.cards.s.SuturePriest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TocatliHonorGuard.class, GrizzlyBears.class, PriestOfUrabrask.class, SuturePriest.class,
        HierophantsChalice.class, LegionsLanding.class, MarchOfTheMachines.class})
class TocatliHonorGuardTest extends BaseCardTest {


    @Test
    @DisplayName("Suppresses a creature's own ETB triggered ability (Priest of Urabrask gets no mana)")
    void suppressesCreatureOwnETBTriggeredAbility() {
        harness.addToBattlefield(player1, new TocatliHonorGuard());

        harness.setHand(player1, List.of(new PriestOfUrabrask()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell — Priest enters but ETB does not trigger
        harness.passBothPriorities();

        // Priest is on the battlefield
        harness.assertOnBattlefield(player1, "Priest of Urabrask");
        // Stack is empty — no triggered ability was placed on it
        assertThat(gd.stack).isEmpty();
        // No mana was awarded (ETB suppressed)
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(0);
    }


    @Test
    @DisplayName("Suppresses Suture Priest's ally creature trigger")
    void suppressesSuturePriestAllyTrigger() {
        harness.addToBattlefield(player1, new TocatliHonorGuard());
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell — Grizzly Bears enters, but Suture Priest does NOT trigger
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Suppresses Suture Priest's opponent creature trigger")
    void suppressesSuturePriestOpponentTrigger() {
        harness.addToBattlefield(player1, new TocatliHonorGuard());
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setLife(player2, 20);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);
        harness.castCreature(player2, 0);

        // Resolve creature spell — Grizzly Bears enters under player2, Suture Priest does NOT trigger
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }


    @Test
    @DisplayName("After Tocatli Honor Guard leaves the battlefield, ETB triggers work normally")
    void etbTriggersWorkAfterRemoval() {
        TocatliHonorGuard guard = new TocatliHonorGuard();
        harness.addToBattlefield(player1, guard);

        // Remove the guard from the battlefield
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Tocatli Honor Guard"));

        // Now cast a creature with ETB — it should trigger normally
        harness.setHand(player1, List.of(new PriestOfUrabrask()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);

        // Resolve creature spell — Priest enters, ETB goes on stack
        harness.passBothPriorities();
        // Resolve ETB trigger — 3 red mana is added
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Priest of Urabrask");
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }


    @Test
    @DisplayName("Suppresses ETB triggers for opponent's creatures too")
    void suppressesOpponentCreatureETB() {
        harness.addToBattlefield(player1, new TocatliHonorGuard());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player2, List.of(new PriestOfUrabrask()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castCreature(player2, 0);

        // Resolve creature spell — Priest enters but ETB does not trigger
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Priest of Urabrask");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.RED)).isEqualTo(0);
    }

    @Test
    void suppressesTriggersFromItsOwnArrival() {
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setHand(player1, List.of(new TocatliHonorGuard()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Tocatli Honor Guard");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    void doesNotRemoveAnAlreadyTriggeredAbility() {
        harness.setHand(player1, List.of(new PriestOfUrabrask()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);

        harness.enterBattlefieldAndReturn(player1, new TocatliHonorGuard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(3);
    }

    @Test
    void noncreatureEntryTriggersStillWorkButCreatureTokenEntryDoesNotTrigger() {
        harness.addToBattlefield(player1, new TocatliHonorGuard());
        harness.addToBattlefield(player1, new SuturePriest());
        harness.setHand(player1, List.of(new LegionsLanding()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castEnchantment(player1, 0);
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Vampire");
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        harness.assertLife(player1, 20);
    }

    @Test
    void noncreatureArtifactEntryAbilityStillTriggers() {
        harness.addToBattlefield(player1, new TocatliHonorGuard());
        harness.setHand(player1, List.of(new HierophantsChalice()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void suppressesArtifactEntryAbilityWhenContinuousEffectMakesItACreature() {
        harness.addToBattlefield(player1, new TocatliHonorGuard());
        harness.addToBattlefield(player1, new MarchOfTheMachines());
        harness.setHand(player1, List.of(new HierophantsChalice()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Hierophant's Chalice");
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }
}
