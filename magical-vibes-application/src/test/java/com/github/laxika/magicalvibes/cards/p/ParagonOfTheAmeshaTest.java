package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CardSubtype;
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

class ParagonOfTheAmeshaTest extends BaseCardTest {

    @Test
    @DisplayName("Ability makes it an Angel with +3/+3, flying, and lifelink")
    void abilityGrantsAngelBuffsAndKeywords() {
        Permanent paragon = addParagon(player1);
        addWubrg(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(paragon.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.ANGEL);
        assertThat(gqs.getEffectivePower(gd, paragon)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, paragon)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, paragon, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, paragon, Keyword.LIFELINK)).isTrue();
    }

    @Test
    @DisplayName("Activating twice stacks the +3/+3 boosts")
    void activatingTwiceStacksBoosts() {
        Permanent paragon = addParagon(player1);
        addWubrg(player1);
        addWubrg(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, paragon)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, paragon)).isEqualTo(8);
    }

    @Test
    @DisplayName("Everything wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent paragon = addParagon(player1);
        addWubrg(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(paragon.getTransientCreatureTypeOverride()).isNull();
        assertThat(gqs.getEffectivePower(gd, paragon)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, paragon)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, paragon, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, paragon, Keyword.LIFELINK)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without the full five-color cost")
    void cannotActivateWithoutFullCost() {
        addParagon(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addParagon(Player player) {
        Permanent perm = new Permanent(new ParagonOfTheAmesha());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addWubrg(Player player) {
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.addMana(player, ManaColor.BLUE, 1);
        harness.addMana(player, ManaColor.BLACK, 1);
        harness.addMana(player, ManaColor.RED, 1);
        harness.addMana(player, ManaColor.GREEN, 1);
    }
}
