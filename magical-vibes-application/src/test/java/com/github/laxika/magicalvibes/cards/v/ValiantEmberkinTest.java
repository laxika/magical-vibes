package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.c.Cowardice;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.r.Retromancer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValiantEmberkin.class, GrizzlyBears.class, Retromancer.class, Humble.class, Cowardice.class})
class ValiantEmberkinTest extends BaseCardTest {

    @Test
    void entersAndPerpetuallyBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new ValiantEmberkin()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0, target.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void attacksAndPerpetuallyBoostsTargetCreature() {
        addCreatureReady(player1, new ValiantEmberkin());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    void doublesTriggeredAbilitiesWhenAnAllyCreatureBecomesTargeted() {
        addCreatureReady(player1, new ValiantEmberkin());
        Permanent retromancer = addCreatureReady(player1, new Retromancer());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new Humble()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castInstant(player2, 0, retromancer.getId());

        assertThat(gd.stack).hasSize(3);
        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals(retromancer.getCard().getName()))
                .count()).isEqualTo(2);

        resolveAllTriggers();
        harness.assertLife(player2, 14);
    }

    @Test
    void perpetualBoostAppliesAfterBasePowerIsSet() {
        addCreatureReady(player1, new ValiantEmberkin());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    void doublesAnEnchantmentTriggerCausedByTargetingYourCreature() {
        addCreatureReady(player1, new ValiantEmberkin());
        harness.addToBattlefield(player1, new Cowardice());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack.stream()
                .filter(entry -> entry.getCard() instanceof Cowardice)
                .count()).isEqualTo(2);
        resolveAllTriggers();
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void doesNotDoubleAnOpponentsTargetTriggeredAbility() {
        addCreatureReady(player1, new ValiantEmberkin());
        Permanent target = addCreatureReady(player2, new Retromancer());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 17);
    }

    @Test
    void twoEmberkinsEachAddOneTriggerWhenTheirAttackAbilityTargetsAnAlly() {
        addCreatureReady(player1, new ValiantEmberkin());
        addCreatureReady(player1, new ValiantEmberkin());
        Permanent target = addCreatureReady(player1, new Retromancer());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.assertLife(player1, 11);
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
    }

    @Test
    void canTargetItselfWithItsAttackAbility() {
        Permanent emberkin = addCreatureReady(player1, new ValiantEmberkin());

        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, emberkin.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, emberkin)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, emberkin)).isEqualTo(2);
    }

    @Test
    void perpetualBoostSurvivesReturningToHandAndBeingCastAgain() {
        addCreatureReady(player1, new ValiantEmberkin());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        declareAttackers(List.of(0));
        harness.handlePermanentChosen(player1, target.getId());
        resolveAllTriggers();

        harness.addToBattlefield(player1, new Cowardice());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent returned = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, returned)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, returned)).isEqualTo(2);
    }
}
