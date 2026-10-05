package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.FlyingMen;
import com.github.laxika.magicalvibes.cards.s.SpittingSlug;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Pendelhaven.class, FlyingMen.class, SpittingSlug.class})
class PendelhavenTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Pendelhaven adds one green mana")
    void tapsForGreenMana() {
        Permanent pendelhaven = addPendelhavenReady(player1);

        harness.activateAbility(player1, 0, null, null);

        assertThat(pendelhaven.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability gives a 1/1 creature +1/+2 until end of turn")
    void boostsOneOneCreature() {
        addPendelhavenReady(player1);
        Permanent flyingMen = addCreatureReady(player1, new FlyingMen());

        harness.activateAbility(player1, 0, 1, null, flyingMen.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, flyingMen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, flyingMen)).isEqualTo(3);
    }

    @Test
    @DisplayName("Boost wears off at end of turn")
    void boostWearsOff() {
        addPendelhavenReady(player1);
        Permanent flyingMen = addCreatureReady(player1, new FlyingMen());

        harness.activateAbility(player1, 0, 1, null, flyingMen.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, flyingMen)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, flyingMen)).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability cannot target a creature that is not 1/1")
    void cannotTargetNonOneOneCreature() {
        addPendelhavenReady(player1);
        Permanent slug = addCreatureReady(player1, new SpittingSlug());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, slug.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a 1/1 creature");
    }

    @Test
    @DisplayName("Ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent pendelhaven = addPendelhavenReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, pendelhaven.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a 1/1 creature");
    }

    @Test
    @DisplayName("Ability can target an opponent's 1/1 creature")
    void canTargetOpponentCreature() {
        addPendelhavenReady(player1);
        Permanent flyingMen = addCreatureReady(player2, new FlyingMen());

        harness.activateAbility(player1, 0, 1, null, flyingMen.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, flyingMen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, flyingMen)).isEqualTo(3);
    }

    @Test
    @DisplayName("Ability rechecks that the target is 1/1 when it resolves")
    void targetMustStillBeOneOneWhenAbilityResolves() {
        Permanent firstPendelhaven = addPendelhavenReady(player1);
        Permanent secondPendelhaven = addPendelhavenReady(player2);
        Permanent flyingMen = addCreatureReady(player1, new FlyingMen());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(firstPendelhaven),
                1, null, flyingMen.getId());
        harness.passPriority(player1);
        harness.activateAbility(player2,
                gd.playerBattlefields.get(player2.getId()).indexOf(secondPendelhaven),
                1, null, flyingMen.getId());
        resolveAllTriggers();

        assertThat(gqs.getEffectivePower(gd, flyingMen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, flyingMen)).isEqualTo(3);
    }

    @ParameterizedTest
    @CsvSource({"1, 0", "0, 1", "-1, 0"})
    @DisplayName("Target restriction checks current power and toughness independently")
    void cannotTargetCreatureWithOnlyOneStatEqualToOne(int powerModifier, int toughnessModifier) {
        Permanent pendelhaven = addPendelhavenReady(player1);
        Permanent flyingMen = addCreatureReady(player1, new FlyingMen());
        flyingMen.setPowerModifier(powerModifier);
        flyingMen.setToughnessModifier(toughnessModifier);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, flyingMen.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a 1/1 creature");

        assertThat(pendelhaven.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Boost resolves after Pendelhaven leaves the battlefield")
    void boostResolvesWithoutSource() {
        Permanent pendelhaven = addPendelhavenReady(player1);
        Permanent flyingMen = addCreatureReady(player1, new FlyingMen());

        harness.activateAbility(player1, 0, 1, null, flyingMen.getId());
        gd.playerBattlefields.get(player1.getId()).remove(pendelhaven);
        gd.playerGraveyards.get(player1.getId()).add(pendelhaven.getCard());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, flyingMen)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, flyingMen)).isEqualTo(3);
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addPendelhavenReady(Player player) {
        return harness.addToBattlefieldAndReturn(player, new Pendelhaven());
    }
}
