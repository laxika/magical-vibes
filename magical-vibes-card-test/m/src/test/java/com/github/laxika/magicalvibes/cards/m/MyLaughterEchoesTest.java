package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.b.BeholdThePowerOfDestruction;
import com.github.laxika.magicalvibes.cards.f.FearMyAuthority;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.ICallForSlaughter;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyLaughterEchoes.class, ICallForSlaughter.class, FearMyAuthority.class,
        BeholdThePowerOfDestruction.class, GrizzlyBears.class})
class MyLaughterEchoesTest extends BaseCardTest {

    @Test
    void mayAbandonAndRepeatNonOngoingScheme() {
        PermanentSetup setup = setUpEchoesAndScheme();

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(setup.echoes());
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(setup.echoes().getCard());
        assertThat(countPermanents(player1, "Devil")).isEqualTo(6);
    }

    @Test
    void repeatedSchemeChoosesItsTargetAgain() {
        harness.addToBattlefieldAndReturn(player1, new MyLaughterEchoes());
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        BeholdThePowerOfDestruction scheme = new BeholdThePowerOfDestruction();
        StackEntry schemeEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getCastingCostsAndSpellEffects());
        schemeEntry.setTargetId(player2.getId());
        gd.stack.add(schemeEntry);
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkSchemeSetInMotionTriggers(gd, schemeEntry));

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        // Setting it in motion again triggers its ability again, whose target is chosen as it goes on the stack.
        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice).as("the repeated ability chooses its target").isNotNull();
        assertThat(targetChoice.validPlayerIds()).containsExactly(player2.getId());
        harness.handlePermanentChosen(player1, player2.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
    }

    @Test
    void decliningKeepsOngoingSchemeAndDoesNotRepeat() {
        PermanentSetup setup = setUpEchoesAndScheme();

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(setup.echoes());
        assertThat(countPermanents(player1, "Devil")).isEqualTo(3);
    }

    @Test
    void doesNotTriggerForAnOngoingScheme() {
        harness.addToBattlefield(player1, new MyLaughterEchoes());
        FearMyAuthority scheme = new FearMyAuthority();
        StackEntry schemeEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getCastingCostsAndSpellEffects());
        gd.stack.add(schemeEntry);

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkSchemeSetInMotionTriggers(gd, schemeEntry));

        assertThat(gd.stack).containsExactly(schemeEntry);
    }

    @Test
    void doesNotTriggerForAnotherPlayersScheme() {
        var echoes = harness.addToBattlefieldAndReturn(player1, new MyLaughterEchoes());
        ICallForSlaughter scheme = new ICallForSlaughter();
        StackEntry schemeEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player2.getId(),
                scheme.getName(),
                scheme.getCastingCostsAndSpellEffects());
        gd.stack.add(schemeEntry);

        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkSchemeSetInMotionTriggers(gd, schemeEntry));

        assertThat(gd.stack).containsExactly(schemeEntry);
        resolveAllTriggers();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(echoes);
        assertThat(countPermanents(player1, "Devil")).isZero();
        assertThat(countPermanents(player2, "Devil")).isEqualTo(3);
    }

    @Test
    void cannotRepeatIfThisSchemeIsNoLongerPresent() {
        PermanentSetup setup = setUpEchoesAndScheme();
        gd.playerBattlefields.get(player1.getId()).remove(setup.echoes());

        resolveAllTriggers();
        if (gd.interaction.activeInteraction() instanceof PendingInteraction.MayAbilityChoice) {
            harness.handleMayAbilityChosen(player1, true);
            resolveAllTriggers();
        }

        assertThat(countPermanents(player1, "Devil")).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void faceUpSchemeInCommandZoneMayAbandonAndRepeatNonOngoingScheme() {
        MyLaughterEchoes echoes = new MyLaughterEchoes();
        gd.playerCommandZones.get(player1.getId()).add(echoes);
        setSchemeInMotion(new ICallForSlaughter());

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.faceDownCommandZoneCards).contains(echoes.getId());
        assertThat(countPermanents(player1, "Devil")).isEqualTo(6);
    }

    private void setSchemeInMotion(ICallForSlaughter scheme) {
        StackEntry schemeEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getCastingCostsAndSpellEffects());
        gd.stack.add(schemeEntry);
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkSchemeSetInMotionTriggers(gd, schemeEntry));
    }

    private PermanentSetup setUpEchoesAndScheme() {
        var echoes = harness.addToBattlefieldAndReturn(player1, new MyLaughterEchoes());
        ICallForSlaughter scheme = new ICallForSlaughter();
        StackEntry schemeEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getCastingCostsAndSpellEffects());
        gd.stack.add(schemeEntry);
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkSchemeSetInMotionTriggers(gd, schemeEntry));
        return new PermanentSetup(echoes);
    }

    private record PermanentSetup(Permanent echoes) {}
}
