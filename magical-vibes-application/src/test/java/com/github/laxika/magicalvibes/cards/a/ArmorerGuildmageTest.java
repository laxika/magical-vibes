package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ArmorerGuildmage.class, Forest.class})
class ArmorerGuildmageTest extends BaseCardTest {

    @Test
    @DisplayName("{B}, {T}: target creature gets +1/+0 until end of turn")
    void boostsPower() {
        Permanent guildmage = addCreatureReady(player1, new ArmorerGuildmage());
        Permanent target = addCreatureReady(player2, new ArmorerGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("{G}, {T}: target creature gets +0/+1 until end of turn")
    void boostsToughness() {
        Permanent guildmage = addCreatureReady(player1, new ArmorerGuildmage());
        Permanent target = addCreatureReady(player1, new ArmorerGuildmage());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(guildmage.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isZero();
        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
    }

    @Test
    @DisplayName("The +1/+0 boost wears off at end of turn")
    void boostWearsOff() {
        addCreatureReady(player1, new ArmorerGuildmage());
        Permanent target = addCreatureReady(player1, new ArmorerGuildmage());
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The +0/+1 boost wears off at end of turn")
    void toughnessBoostWearsOff() {
        addCreatureReady(player1, new ArmorerGuildmage());
        Permanent target = addCreatureReady(player1, new ArmorerGuildmage());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(1);
    }

    @Test
    @DisplayName("The power boost cannot target a noncreature permanent")
    void powerBoostRejectsNonCreaturePermanent() {
        addCreatureReady(player1, new ArmorerGuildmage());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.BLACK, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("The toughness boost cannot target a noncreature permanent")
    void toughnessBoostRejectsNonCreaturePermanent() {
        addCreatureReady(player1, new ArmorerGuildmage());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
