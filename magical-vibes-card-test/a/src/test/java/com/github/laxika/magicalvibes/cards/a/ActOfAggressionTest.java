package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.PorcelainLegionnaire;
import com.github.laxika.magicalvibes.cards.s.ShrineOfBurningRage;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ActOfAggression.class, PorcelainLegionnaire.class, ShrineOfBurningRage.class})
class ActOfAggressionTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Act of Aggression puts it on the stack with the target creature")
    void castingPutsOnStack() {
        Permanent target = addCreatureReady(player2, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Act of Aggression");
        assertThat(entry.getTargetId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving Act of Aggression untaps target, gains control, and grants haste")
    void resolvesUntapGainControlAndHaste() {
        Permanent target = addCreatureReady(player2, new PorcelainLegionnaire());
        target.tap();
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isTrue();
    }

    @Test
    @DisplayName("Stolen creature can attack this turn because Act of Aggression grants haste")
    void stolenCreatureCanAttackDueToHaste() {
        Permanent target = addCreatureReady(player2, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);

        declareAttackers(player1, List.of(attackerIndex));

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addCreatureReady(player2, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.isStolenUntilEndOfTurn(target.getId())).isFalse();
    }

    @Test
    @DisplayName("Cannot target own creature")
    void cannotTargetOwnCreature() {
        addCreatureReady(player2, new PorcelainLegionnaire()); // valid target so spell is playable
        Permanent ownCreature = addCreatureReady(player1, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, ownCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        addCreatureReady(player2, new PorcelainLegionnaire()); // valid target so spell is playable
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ShrineOfBurningRage());
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, artifact.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature an opponent controls");
    }

    @Test
    @DisplayName("Can be cast with only nonred mana by paying 4 life for Phyrexian mana")
    void canPayPhyrexianManaWithLife() {
        Permanent target = addCreatureReady(player2, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new ActOfAggression()));
        // Only 3 white mana — no red. Phyrexian {R/P}{R/P} must be paid with 4 life.
        harness.addMana(player1, ManaColor.WHITE, 3);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        // Card resolved successfully
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        // Player paid 4 life (2 per Phyrexian symbol)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Pays Phyrexian mana with red mana when available instead of life")
    void paysPhyrexianWithManaWhenAvailable() {
        Permanent target = addCreatureReady(player2, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new ActOfAggression()));
        // 3 white + 2 red — enough to pay entirely with mana
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        // Card resolved successfully, no life paid
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Pays partial Phyrexian mana with red and the rest with life")
    void paysPartialPhyrexianWithManaAndLife() {
        Permanent target = addCreatureReady(player2, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new ActOfAggression()));
        // 3 white + 1 red — one Phyrexian paid with mana, one with 2 life
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        // Card resolved successfully
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        // Player paid 2 life (1 Phyrexian symbol paid with life)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addCreatureReady(player2, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 5);

        harness.castInstant(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gameLogContains("fizzles")).isTrue();
    }

    @Test
    @DisplayName("An earlier Act of Aggression fizzles after another copy gives its caster control")
    void fizzlesIfTargetNoLongerControlledByOpponent() {
        Permanent target = addCreatureReady(player2, new PorcelainLegionnaire());
        harness.setHand(player1, List.of(new ActOfAggression(), new ActOfAggression()));
        harness.addMana(player1, ManaColor.RED, 10);

        harness.castInstant(player1, 0, target.getId());
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(target);
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        target.tap();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isTapped()).isTrue();
        assertThat(gameLogContains("fizzles")).isTrue();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .filteredOn(card -> card instanceof ActOfAggression).hasSize(2);
    }
}
