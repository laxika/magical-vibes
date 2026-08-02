package com.github.laxika.magicalvibes.cards.h;

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

class HorrorOfTheDimTest extends BaseCardTest {

    @Test
    @DisplayName("{U} ability grants Horror of the Dim hexproof until end of turn")
    void abilityGrantsHexproofUntilEndOfTurn() {
        Permanent horror = addHorrorReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, horror, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("Hexproof from the ability wears off at end of turn")
    void hexproofWearsOffAtEndOfTurn() {
        Permanent horror = addHorrorReady(player1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, horror, Keyword.HEXPROOF)).isFalse();
    }

    @Test
    @DisplayName("The ability requires one blue mana")
    void abilityRequiresBlueMana() {
        addHorrorReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addHorrorReady(Player player) {
        Permanent horror = new Permanent(new HorrorOfTheDim());
        horror.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(horror);
        return horror;
    }
}
