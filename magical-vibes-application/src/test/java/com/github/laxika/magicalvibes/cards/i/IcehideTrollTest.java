package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.d.DemonBolt;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IcehideTroll.class, DemonBolt.class})
class IcehideTrollTest extends BaseCardTest {

    @Test
    @DisplayName("Snow mana activates the ability, boosts the Troll, grants indestructible, and taps it")
    void activatesWithSnowMana() {
        Permanent troll = addReadyTroll(player1);
        addSnowMana(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, troll, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(troll.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability requires snow mana")
    void requiresSnowMana() {
        addReadyTroll(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    @DisplayName("The boost and indestructible wear off at end of turn")
    void temporaryEffectsWearOffAtEndOfTurn() {
        Permanent troll = addReadyTroll(player1);
        addSnowMana(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, troll, Keyword.INDESTRUCTIBLE)).isFalse();
    }

    @Test
    void tapsOnlyWhenAbilityResolves() {
        Permanent troll = addReadyTroll(player1);
        addSnowMana(player1, 2);

        harness.activateAbility(player1, 0, null, null);

        assertThat(troll.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, troll, Keyword.INDESTRUCTIBLE)).isFalse();

        harness.passBothPriorities();

        assertThat(troll.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, troll, Keyword.INDESTRUCTIBLE)).isTrue();
    }

    @Test
    void activatesWhileSummoningSick() {
        Permanent troll = harness.addToBattlefieldAndReturn(player1, new IcehideTroll());
        troll.setSummoningSick(true);
        addSnowMana(player1, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, troll, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(troll.isTapped()).isTrue();
    }

    @Test
    void activatesAgainWhileTappedAndBoostsStack() {
        Permanent troll = addReadyTroll(player1);
        addSnowMana(player1, 4);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, troll)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, troll, Keyword.INDESTRUCTIBLE)).isTrue();
        assertThat(troll.isTapped()).isTrue();
    }

    @Test
    void oneSnowManaAndOneOrdinaryManaCannotPay() {
        Permanent troll = addReadyTroll(player1);
        addSnowMana(player1, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");

        assertThat(troll.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void colorlessSnowManaCanPayAndIsConsumed() {
        Permanent troll = addReadyTroll(player1);
        ManaPool pool = gd.playerManaPools.get(player1.getId());
        pool.add(ManaColor.COLORLESS, 2);
        pool.addSnowMana(ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, troll)).isEqualTo(4);
        assertThat(pool.getSnowManaTotal()).isZero();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("mana");
    }

    @Test
    void indestructibleSurvivesLethalDamage() {
        Permanent troll = addReadyTroll(player1);
        addSnowMana(player1, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new DemonBolt()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0, troll.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(troll);
        assertThat(troll.getMarkedDamage()).isEqualTo(4);
    }

    private Permanent addReadyTroll(Player player) {
        return addCreatureReady(player, new IcehideTroll());
    }

    private void addSnowMana(Player player, int amount) {
        ManaPool pool = gd.playerManaPools.get(player.getId());
        pool.add(ManaColor.GREEN, amount);
        pool.addSnowMana(ManaColor.GREEN, amount);
    }
}
