package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CallerOfGales.class})
class CallerOfGalesTest extends BaseCardTest {

    @Test
    @DisplayName("Ability grants flying to target creature")
    void abilityGrantsFlying() {
        addReadyCallerOfGales(player1);
        Permanent target = addReadyCallerOfGales(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Granted flying is removed at end of turn")
    void flyingRemovedAtEndOfTurn() {
        addReadyCallerOfGales(player1);
        Permanent target = addReadyCallerOfGales(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
    }

    private void addAbilityMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
    }

    @Test
    @DisplayName("Ability can target an opponent's creature and taps its source")
    void abilityTargetsOpponentCreature() {
        Permanent source = addReadyCallerOfGales(player1);
        Permanent target = addReadyCallerOfGales(player2);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        assertThat(source.isTapped()).isTrue();
        assertThat(target.hasKeyword(Keyword.FLYING)).isFalse();
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
        assertThat(source.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Caller of Gales can target itself")
    void abilityTargetsItself() {
        Permanent source = addReadyCallerOfGales(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, source.getId());
        harness.passBothPriorities();

        assertThat(source.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Ability resolves after its source leaves the battlefield")
    void abilityResolvesWithoutSource() {
        Permanent source = addReadyCallerOfGales(player1);
        Permanent target = addReadyCallerOfGales(player1);
        addAbilityMana();

        harness.activateAbility(player1, 0, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerGraveyards.get(player1.getId()).add(source.getCard());
        harness.passBothPriorities();

        assertThat(target.hasKeyword(Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Summoning sickness prevents paying the tap cost")
    void summoningSickCallerCannotActivate() {
        Permanent source = harness.addToBattlefieldAndReturn(player1, new CallerOfGales());
        source.setSummoningSick(true);
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.isTapped()).isFalse();
        assertThat(source.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Ability cannot be activated without the full mana cost")
    void insufficientManaPreventsActivation() {
        Permanent source = addReadyCallerOfGales(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.hasKeyword(Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("A tapped Caller of Gales cannot activate its ability")
    void tappedCallerCannotActivate() {
        Permanent source = addReadyCallerOfGales(player1);
        source.tap();
        addAbilityMana();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, source.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(source.hasKeyword(Keyword.FLYING)).isFalse();
    }

    private Permanent addReadyCallerOfGales(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new CallerOfGales());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
