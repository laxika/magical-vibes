package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
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

@CardUsed({WildheartInvoker.class, RagingGoblin.class})
class WildheartInvokerTest extends BaseCardTest {

    @Test
    @DisplayName("Ability gives target creature +5/+5 and trample")
    void boostsTargetCreatureAndGrantsTrample() {
        addInvoker(player1);
        Permanent target = addCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Ability can target an opponent's creature")
    void boostsOpponentsCreature() {
        addInvoker(player1);
        Permanent target = addCreature(player2);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Boost and trample wear off at end of turn")
    void effectWearsOffAtEndOfTurn() {
        addInvoker(player1);
        Permanent target = addCreature(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void canTargetItselfWhileTappedAndSummoningSick() {
        Permanent invoker = harness.addToBattlefieldAndReturn(player1, new WildheartInvoker());
        invoker.setTapped(true);
        invoker.setSummoningSick(true);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, invoker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, invoker)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, invoker)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, invoker, Keyword.TRAMPLE)).isTrue();
        assertThat(invoker.isTapped()).isTrue();
    }

    @Test
    void repeatedActivationsStackTheirBoosts() {
        Permanent invoker = addInvoker(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 16);

        harness.activateAbility(player1, 0, null, invoker.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, invoker.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, invoker)).isEqualTo(14);
        assertThat(gqs.getEffectiveToughness(gd, invoker)).isEqualTo(13);
        assertThat(gqs.hasKeyword(gd, invoker, Keyword.TRAMPLE)).isTrue();
        assertThat(invoker.isTapped()).isFalse();
    }

    @Test
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent invoker = addInvoker(player1);
        Permanent target = harness.addToBattlefieldAndReturn(player2, new WildheartInvoker());
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(invoker);
        gd.playerGraveyards.get(player1.getId()).add(invoker.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(9);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(8);
        assertThat(gqs.hasKeyword(gd, target, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void cannotActivateWithOnlySevenMana() {
        Permanent invoker = addInvoker(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, invoker.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void cannotTargetAPlayer() {
        addInvoker(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 8);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addInvoker(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new WildheartInvoker());
        permanent.setSummoningSick(false);
        return permanent;
    }

    private Permanent addCreature(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new RagingGoblin());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
