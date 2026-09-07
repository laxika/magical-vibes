package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.z.ZodiacDragon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DragonkinBerserkerTest extends BaseCardTest {

    @Test
    @DisplayName("Boast creates a 5/5 red Dragon token with flying")
    void boastCreatesDragonToken() {
        Permanent berserker = addCreatureReady(player1, new DragonkinBerserker());
        addCreatureReady(player1, new ZodiacDragon());
        berserker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        Permanent token = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken())
                .findFirst()
                .orElseThrow();
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.RED);
        assertThat(token.getCard().getSubtypes()).containsExactly(CardSubtype.DRAGON);
        assertThat(token.getCard().getKeywords()).contains(Keyword.FLYING);
        assertThat(token.getEffectivePower()).isEqualTo(5);
        assertThat(token.getEffectiveToughness()).isEqualTo(5);
    }

    @Test
    @DisplayName("Each controlled Dragon reduces the boast activation cost")
    void eachDragonReducesBoastCost() {
        Permanent berserker = addCreatureReady(player1, new DragonkinBerserker());
        addCreatureReady(player1, new ZodiacDragon());
        addCreatureReady(player1, new ZodiacDragon());
        berserker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Boast cannot be activated before Dragonkin Berserker attacks")
    void boastRequiresAttack() {
        addCreatureReady(player1, new DragonkinBerserker());
        addCreatureReady(player1, new ZodiacDragon());
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this turn");
    }

    @Test
    @DisplayName("Boast can be activated only once each turn")
    void boastOnlyOncePerTurn() {
        Permanent berserker = addCreatureReady(player1, new DragonkinBerserker());
        addCreatureReady(player1, new ZodiacDragon());
        berserker.setAttackedThisTurn(true);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("only once each turn");
    }
}
