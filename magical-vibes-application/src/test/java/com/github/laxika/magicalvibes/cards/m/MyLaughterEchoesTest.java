package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.FearMyAuthority;
import com.github.laxika.magicalvibes.cards.i.ICallForSlaughter;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MyLaughterEchoes.class, ICallForSlaughter.class, FearMyAuthority.class})
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
                scheme.getEffects(EffectSlot.SPELL));
        gd.stack.add(schemeEntry);

        harness.getTriggerCollectionService().checkSchemeSetInMotionTriggers(gd, schemeEntry);

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
                scheme.getEffects(EffectSlot.SPELL));
        gd.stack.add(schemeEntry);

        harness.getTriggerCollectionService().checkSchemeSetInMotionTriggers(gd, schemeEntry);

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

    private PermanentSetup setUpEchoesAndScheme() {
        var echoes = harness.addToBattlefieldAndReturn(player1, new MyLaughterEchoes());
        ICallForSlaughter scheme = new ICallForSlaughter();
        StackEntry schemeEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                scheme,
                player1.getId(),
                scheme.getName(),
                scheme.getEffects(EffectSlot.SPELL));
        gd.stack.add(schemeEntry);
        harness.getTriggerCollectionService().checkSchemeSetInMotionTriggers(gd, schemeEntry);
        return new PermanentSetup(echoes);
    }

    private record PermanentSetup(Permanent echoes) {}
}
