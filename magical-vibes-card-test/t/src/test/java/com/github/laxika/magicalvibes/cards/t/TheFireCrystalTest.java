package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningStrike;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheFireCrystal.class, LightningStrike.class, GrizzlyBears.class})
class TheFireCrystalTest extends BaseCardTest {

    @Test
    @DisplayName("Red spells you cast cost {1} less")
    void reducesRedSpellCost() {
        harness.addToBattlefield(player1, new TheFireCrystal());
        harness.setHand(player1, java.util.List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Creatures you control have haste")
    void grantsHasteToOwnCreatures() {
        harness.addToBattlefield(player1, new TheFireCrystal());
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.HASTE)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Creates a creature-copy token that is sacrificed at the next end step")
    void createsCopyTokenAndSacrificesItAtNextEndStep() {
        Permanent crystal = addReadyCrystal(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.activateAbility(player1, battlefieldIndex(player1, crystal), null, target.getId());
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getId()).isNotEqualTo(target.getCard().getId());
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(gqs.getEffectivePower(gd, target));
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(gqs.getEffectiveToughness(gd, target));
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getId().equals(token.getId()));
    }

    @Test
    @DisplayName("Cannot target a creature an opponent controls")
    void cannotCopyOpponentCreature() {
        Permanent crystal = addReadyCrystal(player1);
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, crystal), null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

    @Test
    void doesNotReduceNonredSpellCost() {
        harness.addToBattlefield(player1, new TheFireCrystal());
        harness.setHand(player1, java.util.List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReduceOpponentsRedSpellCost() {
        harness.addToBattlefield(player2, new TheFireCrystal());
        harness.setHand(player1, java.util.List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void reductionDoesNotPayColoredMana() {
        harness.addToBattlefield(player1, new TheFireCrystal());
        harness.setHand(player1, java.util.List.of(new LightningStrike()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReduceItsActivatedAbilityCost() {
        Permanent crystal = addReadyCrystal(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, crystal), null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
        assertThat(crystal.isTapped()).isFalse();
    }

    @Test
    void cannotCopyNoncreaturePermanent() {
        Permanent crystal = addReadyCrystal(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(player1, crystal), null, crystal.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void copyAbilityFailsWhenTargetLeavesBeforeResolution() {
        Permanent crystal = addReadyCrystal(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, battlefieldIndex(player1, crystal), null, target.getId());
        assertThat(crystal.isTapped()).isTrue();
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, target));

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().isToken());
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokenDoesNotKeepCrystalsHasteAfterCrystalLeaves() {
        Permanent crystal = addReadyCrystal(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, battlefieldIndex(player1, crystal), null, target.getId());
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, crystal));

        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
        assertThat(gqs.hasKeyword(gd, target, Keyword.HASTE)).isFalse();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    void tokenCreatedDuringEndStepSurvivesUntilNextEndStep() {
        harness.forceStep(TurnStep.END_STEP);
        Permanent crystal = addReadyCrystal(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, battlefieldIndex(player1, crystal), null, target.getId());
        harness.passBothPriorities();
        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();

        harness.passUntilWithNoAttackers(player2, TurnStep.POSTCOMBAT_MAIN);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        harness.passUntil(player2, TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    @Test
    void copyDoesNotCopyCountersOrTappedStatus() {
        Permanent crystal = addReadyCrystal(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        int originalPower = gqs.getEffectivePower(gd, target);
        int originalToughness = gqs.getEffectiveToughness(gd, target);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        target.tap();
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, battlefieldIndex(player1, crystal), null, target.getId());

        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(token.isTapped()).isFalse();
        assertThat(token.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(originalPower);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(originalToughness);
    }

    @Test
    void copyAbilityResolvesAfterCrystalLeavesBattlefield() {
        Permanent crystal = addReadyCrystal(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, battlefieldIndex(player1, crystal), null, target.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, crystal));

        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()).findFirst().orElseThrow();
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(gqs.getEffectivePower(gd, target));
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(gqs.getEffectiveToughness(gd, target));
        assertThat(gqs.hasKeyword(gd, token, Keyword.HASTE)).isFalse();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(token);
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(token);
    }

    private Permanent addReadyCrystal(Player player) {
        Permanent crystal = harness.addToBattlefieldAndReturn(player, new TheFireCrystal());
        crystal.setSummoningSick(false);
        return crystal;
    }

    private int battlefieldIndex(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }
}
