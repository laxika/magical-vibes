package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.c.ConspiracyUnraveler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.Humility;
import com.github.laxika.magicalvibes.cards.s.Stifle;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LightningBerserker.class, Stifle.class, Humility.class, TurnToFrog.class, ConspiracyUnraveler.class,
        GrizzlyBears.class})
class LightningBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Paying {R} gives Lightning Berserker +1/+0 until end of turn")
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent berserker = addCreatureReady(player1, new LightningBerserker());
        harness.addMana(player1, ManaColor.RED, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(1);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(3);
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(gqs.getEffectivePower(gd, berserker)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, berserker)).isEqualTo(1);
    }

    @Test
    @DisplayName("Normal cast does not grant haste or return the creature at end step")
    void normalCastDoesNotUseDash() {
        harness.setHand(player1, List.of(new LightningBerserker()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent berserker = findPermanent(player1, "Lightning Berserker");
        assertThat(gqs.hasKeyword(gd, berserker, Keyword.HASTE)).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(findPermanent(player1, "Lightning Berserker")).isSameAs(berserker);
    }

    @Test
    @DisplayName("Dash grants haste and returns Lightning Berserker to its owner's hand at end step")
    void dashGrantsHasteAndReturnsAtEndStep() {
        harness.setHand(player1, List.of(new LightningBerserker()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent berserker = findPermanent(player1, "Lightning Berserker");
        assertThat(gqs.hasKeyword(gd, berserker, Keyword.HASTE)).isTrue();
        assertThat(als.canAttack(gd, berserker, player1.getId())).isTrue();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);

        harness.assertOnBattlefield(player1, "Lightning Berserker");
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        harness.assertInHand(player1, "Lightning Berserker");
        harness.assertNotOnBattlefield(player1, "Lightning Berserker");
    }

    @Test
    @DisplayName("A dashed creature keeps haste past this turn while it stays on the battlefield")
    void dashHasteLastsWhileOnTheBattlefield() {
        harness.forceActivePlayer(player1);
        harness.setHand(player1, List.of(new LightningBerserker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();
        Permanent berserker = findPermanent(player1, "Lightning Berserker");

        harness.setHand(player1, List.of(new Stifle()));
        harness.setHand(player2, List.of());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, gd.stack.getLast().getTargetableId());
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(findPermanent(player1, "Lightning Berserker")).isSameAs(berserker);
        assertThat(gqs.hasKeyword(gd, berserker, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Under an earlier Humility a dashed creature has no haste and can't attack")
    void earlierHumilityRemovesDashHaste() {
        harness.addToBattlefield(player1, new Humility());
        Permanent berserker = dashBerserker();

        assertThat(gqs.hasKeyword(gd, berserker, Keyword.HASTE)).isFalse();
        assertThat(als.canAttack(gd, berserker, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("A later loses-all-abilities effect removes dash's haste")
    void laterAbilityLossRemovesDashHaste() {
        Permanent berserker = dashBerserker();
        assertThat(gqs.hasKeyword(gd, berserker, Keyword.HASTE)).isTrue();

        harness.setHand(player1, List.of(new TurnToFrog()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, berserker.getId());

        assertThat(gqs.hasKeyword(gd, berserker, Keyword.HASTE)).isFalse();
        assertThat(als.canAttack(gd, berserker, player1.getId())).isFalse();
    }

    @Test
    @DisplayName("Cast for a different alternative cost, it neither gets dash's haste nor returns at end step")
    void otherAlternativeCostIsNotDash() {
        harness.forceActivePlayer(player1);
        harness.addToBattlefield(player1, new ConspiracyUnraveler());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new LightningBerserker()));
        harness.setHand(player2, List.of());
        harness.castCreatureWithMultipleGraveyardExile(player1, 0, List.of(0, 1, 2, 3, 4));
        assertThat(gd.stack.getLast().isAlternateCost()).isTrue();
        harness.passBothPriorities();
        Permanent berserker = findPermanent(player1, "Lightning Berserker");

        assertThat(gqs.hasKeyword(gd, berserker, Keyword.HASTE)).isFalse();
        assertThat(als.canAttack(gd, berserker, player1.getId())).isFalse();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(findPermanent(player1, "Lightning Berserker")).isSameAs(berserker);
    }

    private Permanent dashBerserker() {
        harness.setHand(player1, List.of(new LightningBerserker()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();
        return findPermanent(player1, "Lightning Berserker");
    }

    @Test
    @DisplayName("Resolving a dashed creature does not create an enters-the-battlefield trigger")
    void dashReturnIsScheduledByTheSpellWithoutAnEtbTrigger() {
        harness.setHand(player1, List.of(new LightningBerserker()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castWithAlternateCost(player1, 0, (UUID) null);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Lightning Berserker");
        assertThat(gd.stack).isEmpty();
    }
}
