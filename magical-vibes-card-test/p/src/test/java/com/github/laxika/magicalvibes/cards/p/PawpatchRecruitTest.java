package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mutavault;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PawpatchRecruit.class, GrizzlyBears.class, Shock.class, ProdigalPyromancer.class, Mutavault.class})
class PawpatchRecruitTest extends BaseCardTest {

    @Test
    void offspringCreatesOneOneTokenCopyWhenPaid() {
        harness.setHand(player1, List.of(new PawpatchRecruit()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> tokens = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .toList();
        assertThat(tokens).hasSize(1);
        assertThat(tokens.getFirst().getEffectivePower()).isEqualTo(1);
        assertThat(tokens.getFirst().getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void opponentTargetingQueuesChoiceForAnotherCreatureYouControl() {
        Permanent recruit = addCreatureReady(player1, new PawpatchRecruit());
        Permanent targeted = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, targeted.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(recruit.getId())
                .doesNotContain(targeted.getId());

        harness.handlePermanentChosen(player1, recruit.getId());
        harness.passBothPriorities();

        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(targeted.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void triggerIsSkippedWhenTheTargetedCreatureIsTheOnlyCreatureYouControl() {
        Permanent recruit = addCreatureReady(player1, new PawpatchRecruit());

        harness.forceActivePlayer(player2);
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, recruit.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void unpaidOffspringDoesNotCreateAToken() {
        harness.setHand(player1, List.of(new PawpatchRecruit()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1)
                .allMatch(permanent -> !permanent.getCard().isToken());
    }

    @Test
    void friendlySpellDoesNotTrigger() {
        Permanent recruit = addCreatureReady(player1, new PawpatchRecruit());
        Permanent targeted = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, targeted.getId());

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();
        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void opponentActivatedAbilityTriggersForRecruitItself() {
        Permanent recruit = addCreatureReady(player1, new PawpatchRecruit());
        Permanent recipient = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new ProdigalPyromancer());
        harness.ensurePriority(player2);

        harness.activateAbility(player2, 0, null, recruit.getId());

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .contains(recipient.getId()).doesNotContain(recruit.getId());
        harness.handlePermanentChosen(player1, recipient.getId());
        harness.passBothPriorities();

        assertThat(recipient.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void offspringTokenAlsoTriggersWhenOriginalIsTargeted() {
        harness.setHand(player1, List.of(new PawpatchRecruit()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castKickedCreature(player1, 0);
        resolveAllTriggers();
        Permanent original = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> !permanent.getCard().isToken()).findFirst().orElseThrow();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        harness.castInstant(player2, 0, original.getId());
        harness.handlePermanentChosen(player1, token.getId());
        harness.handlePermanentChosen(player1, token.getId());
        resolveAllTriggers();

        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        harness.assertInGraveyard(player1, "Pawpatch Recruit");
    }

    @Test
    void offspringStillCreatesTokenAfterOriginalDies() {
        harness.setHand(player1, List.of(new PawpatchRecruit()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        Permanent original = findPermanent(player1, "Pawpatch Recruit");
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        harness.castInstant(player2, 0, original.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Pawpatch Recruit");
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        Permanent token = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getEffectivePower()).isEqualTo(1);
        assertThat(token.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    void opponentTargetingAnimatedLandTriggers() {
        Permanent recruit = addCreatureReady(player1, new PawpatchRecruit());
        Permanent land = addCreatureReady(player1, new Mutavault());
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();
        assertThat(gqs.isCreature(gd, land)).isTrue();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.ensurePriority(player2);

        harness.castInstant(player2, 0, land.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, recruit.getId());
        harness.passBothPriorities();
        assertThat(recruit.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }
}
