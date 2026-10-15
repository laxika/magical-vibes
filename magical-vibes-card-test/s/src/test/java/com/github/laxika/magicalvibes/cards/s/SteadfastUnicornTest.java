package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SteadfastUnicorn.class, GrizzlyBears.class})
class SteadfastUnicornTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts creatures you control and grants them vigilance")
    void boostsOwnCreaturesAndGrantsVigilance() {
        Permanent unicorn = addCreatureReady(player1, new SteadfastUnicorn());
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        prepareAbility();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, unicorn)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, unicorn)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, unicorn, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, opponentBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opponentBears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The boost and vigilance wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent ownBears = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new SteadfastUnicorn());
        prepareAbility();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownBears)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownBears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot be activated during an opponent's turn")
    void cannotActivateOnOpponentTurn() {
        addCreatureReady(player1, new SteadfastUnicorn());
        addMana();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("A tapped, summoning-sick Unicorn can activate repeatedly during its own end step")
    void canActivateRepeatedlyDuringOwnEndStep() {
        Permanent unicorn = harness.addToBattlefieldAndReturn(player1, new SteadfastUnicorn());
        unicorn.setSummoningSick(true);
        unicorn.tap();
        prepareAbility();
        addMana();
        harness.forceStep(TurnStep.END_STEP);

        harness.activateAbility(player1, 0, null, null);
        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, unicorn)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, unicorn)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, unicorn, Keyword.VIGILANCE)).isTrue();
        assertThat(unicorn.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Creatures entering before resolution benefit, but later creatures do not")
    void affectsOnlyCreaturesPresentAtResolution() {
        addCreatureReady(player1, new SteadfastUnicorn());
        prepareAbility();
        harness.activateAbility(player1, 0, null, null);
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new SteadfastUnicorn());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new SteadfastUnicorn());

        assertThat(gqs.getEffectivePower(gd, beforeResolution)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.VIGILANCE)).isTrue();
        assertThat(gqs.getEffectivePower(gd, afterResolution)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("The ability resolves even if the Unicorn has left the battlefield")
    void resolvesAfterSourceLeavesBattlefield() {
        Permanent source = addCreatureReady(player1, new SteadfastUnicorn());
        Permanent remaining = addCreatureReady(player1, new SteadfastUnicorn());
        prepareAbility();
        harness.activateAbility(player1, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, remaining)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, remaining)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, remaining, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Activation requires the full four mana, including white")
    void cannotActivateWithoutEnoughMana() {
        addCreatureReady(player1, new SteadfastUnicorn());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Four colorless mana cannot pay the white activation cost")
    void cannotActivateWithoutWhiteMana() {
        addCreatureReady(player1, new SteadfastUnicorn());
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Creatures granted vigilance do not tap when attacking")
    void grantedVigilancePreventsTappingToAttack() {
        Permanent unicorn = addCreatureReady(player1, new SteadfastUnicorn());
        prepareAbility();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThat(unicorn.isAttacking()).isTrue();
        assertThat(unicorn.isTapped()).isFalse();
    }

    private void prepareAbility() {
        addMana();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    private void addMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
