package com.github.laxika.magicalvibes.cards.d;

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

class DragonsoulKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Ability makes it a Dragon with +5/+3, flying, and trample")
    void abilityGrantsDragonBuffsAndKeywords() {
        Permanent knight = addKnight(player1);
        addWubrg(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(knight.getTransientCreatureTypeOverride()).isEqualTo(CardSubtype.DRAGON);
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isTrue();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    @DisplayName("Activating twice stacks the +5/+3 boosts")
    void activatingTwiceStacksBoosts() {
        Permanent knight = addKnight(player1);
        addWubrg(player1);
        addWubrg(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(12);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(8);
    }

    @Test
    @DisplayName("Everything wears off at end of turn")
    void wearsOffAtEndOfTurn() {
        Permanent knight = addKnight(player1);
        addWubrg(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(knight.getTransientCreatureTypeOverride()).isNull();
        assertThat(gqs.getEffectivePower(gd, knight)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, knight)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, knight, Keyword.FLYING)).isFalse();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate without the full five-color cost")
    void cannotActivateWithoutFullCost() {
        addKnight(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addKnight(Player player) {
        Permanent perm = new Permanent(new DragonsoulKnight());
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
