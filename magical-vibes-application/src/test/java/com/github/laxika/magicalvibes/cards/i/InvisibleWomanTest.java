package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BurstOfStrength;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InvisibleWoman.class, BurstOfStrength.class, GrizzlyBears.class})
class InvisibleWomanTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a Wall at beginning of combat after casting a noncreature spell")
    void createsWallAfterCastingNoncreatureSpell() {
        addCreatureReady(player1, new InvisibleWoman());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        Permanent wall = findPermanent(player1, "Wall");
        assertThat(wall.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(wall.getCard().getColor()).isNull();
        assertThat(gqs.getEffectivePower(gd, wall)).isZero();
        assertThat(gqs.getEffectiveToughness(gd, wall)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, wall, Keyword.DEFENDER)).isTrue();
        assertThat(gqs.hasKeyword(gd, wall, Keyword.REACH)).isTrue();
    }

    @Test
    @DisplayName("Does not create a Wall when only a creature spell was cast")
    void doesNotCreateWallAfterCastingCreatureSpell() {
        addCreatureReady(player1, new InvisibleWoman());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        advanceToBeginningOfCombat();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Wall")).isEmpty();
    }

    @Test
    @DisplayName("Paying the attack trigger boosts the target and makes it unblockable")
    void payingAttackTriggerBoostsAndMakesTargetUnblockable() {
        addCreatureReady(player1, new InvisibleWoman());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(0));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(target.isCantBeBlocked()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.isCantBeBlocked()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.WHITE)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isZero();
    }

    @Test
    @DisplayName("Declining the attack trigger leaves the target unchanged")
    void decliningAttackTriggerDoesNothing() {
        addCreatureReady(player1, new InvisibleWoman());
        Permanent target = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    void doesNotTriggerWithoutAnySpellCast() {
        addCreatureReady(player1, new InvisibleWoman());

        advanceToBeginningOfCombat();

        assertThat(gd.stack).isEmpty();
        assertThat(findPermanents(player1, "Wall")).isEmpty();
    }

    @Test
    void castingNoncreatureSpellAfterCombatBeginsDoesNotCreateWall() {
        addCreatureReady(player1, new InvisibleWoman());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        advanceToBeginningOfCombat();
        harness.setHand(player1, List.of(new BurstOfStrength()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(findPermanents(player1, "Wall")).isEmpty();
    }

    @Test
    void multipleAttackersTriggerOnceAndCanBoostOpponentsCreature() {
        addCreatureReady(player1, new InvisibleWoman());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        declareAttackers(List.of(1, 2));
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, target.getId());
        addCreatureReady(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(target.isCantBeBlocked()).isTrue();
    }

    private void advanceToBeginningOfCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.BEGINNING_OF_COMBAT);
    }
}
