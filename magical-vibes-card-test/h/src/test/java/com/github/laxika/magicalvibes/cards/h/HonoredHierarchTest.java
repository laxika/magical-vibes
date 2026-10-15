package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameData;
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

@CardUsed({HonoredHierarch.class})
class HonoredHierarchTest extends BaseCardTest {

    @Test
    @DisplayName("Renown 1 puts a +1/+1 counter on it after unblocked combat damage")
    void renownOnCombatDamage() {
        Permanent hierarch = addCreatureReady(player1, new HonoredHierarch());

        declareAttackers(player1, List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        assertThat(hierarch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hierarch.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("It has no vigilance while it is not renowned")
    void noVigilanceWhileNotRenowned() {
        Permanent hierarch = addCreatureReady(player1, new HonoredHierarch());

        assertThat(gqs.hasKeyword(gd, hierarch, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("It has vigilance while it is renowned")
    void vigilanceWhileRenowned() {
        Permanent hierarch = addCreatureReady(player1, new HonoredHierarch());
        hierarch.setRenowned(true);

        assertThat(gqs.hasKeyword(gd, hierarch, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("The mana ability cannot be activated while it is not renowned")
    void manaAbilityBlockedWhileNotRenowned() {
        addCreatureReady(player1, new HonoredHierarch());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("renowned");
    }

    @Test
    @DisplayName("While renowned, tapping for mana adds one mana of the chosen color")
    void manaAbilityAddsChosenColor() {
        Permanent hierarch = addCreatureReady(player1, new HonoredHierarch());
        hierarch.setRenowned(true);
        GameData gameData = harness.getGameData();

        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(hierarch.isTapped()).isTrue();
        assertThat(gameData.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
    }

    @Test
    @DisplayName("An already renowned Hierarch does not gain another counter from combat damage")
    void renownedCreatureDoesNotRenownAgain() {
        Permanent hierarch = addCreatureReady(player1, new HonoredHierarch());
        hierarch.setRenowned(true);
        hierarch.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        declareAttackers(List.of(0));
        resolveAllTriggers();
        resolveCombat();
        resolveAllTriggers();

        harness.assertLife(player2, 18);
        assertThat(hierarch.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(hierarch.isRenowned()).isTrue();
    }

    @Test
    @DisplayName("Renown enables attacking without tapping and then tapping for mana")
    void renownedAttackerCanTapForMana() {
        Permanent hierarch = addCreatureReady(player1, new HonoredHierarch());
        hierarch.setRenowned(true);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> declareAttackers(List.of(0)));

        assertThat(hierarch.isTapped()).isFalse();
        assertThat(hierarch.isAttacking()).isTrue();
        harness.activateAbility(player1, 0, null, null);
        harness.handleListChoice(player1, "GREEN");

        assertThat(hierarch.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("A +1/+1 counter alone does not make Hierarch renowned")
    void counterWithoutRenownDoesNotGrantAbilities() {
        Permanent hierarch = addCreatureReady(player1, new HonoredHierarch());
        hierarch.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, hierarch, Keyword.VIGILANCE)).isFalse();
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("renowned");
    }
}
