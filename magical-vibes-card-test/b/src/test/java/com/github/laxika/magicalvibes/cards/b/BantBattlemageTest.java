package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BantBattlemage.class})
class BantBattlemageTest extends BaseCardTest {

    @Test
    @DisplayName("Green ability grants trample to target creature")
    void greenAbilityGrantsTrample() {
        addCreatureReady(player1, new BantBattlemage());
        Permanent target = addCreatureReady(player1, new BantBattlemage());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Blue ability grants flying to target creature")
    void blueAbilityGrantsFlying() {
        addCreatureReady(player1, new BantBattlemage());
        Permanent target = addCreatureReady(player1, new BantBattlemage());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted keyword is removed at end of turn")
    void keywordRemovedAtEndOfTurn() {
        addCreatureReady(player1, new BantBattlemage());
        Permanent target = addCreatureReady(player1, new BantBattlemage());
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate green ability without green mana")
    void cannotActivateWithoutMana() {
        addCreatureReady(player1, new BantBattlemage());
        Permanent target = addCreatureReady(player1, new BantBattlemage());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @ParameterizedTest
    @CsvSource({"0, GREEN, TRAMPLE", "1, BLUE, FLYING"})
    void canTargetOpponentAndExpiresAtCleanup(int ability, ManaColor mana, Keyword keyword) {
        Permanent source = addCreatureReady(player1, new BantBattlemage());
        Permanent target = addCreatureReady(player2, new BantBattlemage());
        harness.addMana(player1, mana, 1);

        harness.activateAbility(player1, 0, ability, null, target.getId());
        assertThat(source.isTapped()).isTrue();
        assertThat(target.hasKeyword(keyword)).isFalse();
        harness.passBothPriorities();
        assertThat(target.hasKeyword(keyword)).isTrue();
        assertThat(source.hasKeyword(keyword)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        assertThat(target.hasKeyword(keyword)).isFalse();
    }

    @ParameterizedTest
    @CsvSource({"0, GREEN, TRAMPLE", "1, BLUE, FLYING"})
    void canTargetItself(int ability, ManaColor mana, Keyword keyword) {
        Permanent source = addCreatureReady(player1, new BantBattlemage());
        harness.addMana(player1, mana, 1);

        harness.activateAbility(player1, 0, ability, null, source.getId());
        harness.passBothPriorities();

        assertThat(source.isTapped()).isTrue();
        assertThat(source.hasKeyword(keyword)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"0, GREEN, TRAMPLE", "1, BLUE, FLYING"})
    void resolvesAfterSourceLeavesBattlefield(int ability, ManaColor mana, Keyword keyword) {
        Permanent source = addCreatureReady(player1, new BantBattlemage());
        Permanent target = addCreatureReady(player2, new BantBattlemage());
        harness.addMana(player1, mana, 1);

        harness.activateAbility(player1, 0, ability, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(keyword)).isTrue();
    }

    @ParameterizedTest
    @CsvSource({"0, GREEN", "1, BLUE"})
    void summoningSicknessPreventsActivation(int ability, ManaColor mana) {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new BantBattlemage());
        source.setSummoningSick(true);
        harness.addMana(player1, mana, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, ability, null, source.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @ParameterizedTest
    @CsvSource({"0, GREEN", "1, BLUE"})
    void tappedSourceCannotActivate(int ability, ManaColor mana) {
        Permanent source = addCreatureReady(player1, new BantBattlemage());
        source.tap();
        harness.addMana(player1, mana, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, ability, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @ParameterizedTest
    @CsvSource({"0, BLUE", "1, GREEN"})
    void otherAbilityManaCannotPayCost(int ability, ManaColor wrongMana) {
        Permanent source = addCreatureReady(player1, new BantBattlemage());
        harness.addMana(player1, wrongMana, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, ability, null, source.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @ParameterizedTest
    @CsvSource({"0, GREEN, TRAMPLE", "1, BLUE, FLYING"})
    void returningTargetIsANewObject(int ability, ManaColor mana, Keyword keyword) {
        Permanent source = addCreatureReady(player1, new BantBattlemage());
        Permanent target = addCreatureReady(player2, new BantBattlemage());
        harness.addMana(player1, mana, 1);

        harness.activateAbility(player1, 0, ability, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);
        Permanent returned = harness.addToBattlefieldAndReturn(player2, target.getCard());
        harness.passBothPriorities();

        assertThat(returned.hasKeyword(keyword)).isFalse();
        assertThat(source.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }
}
