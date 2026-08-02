package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SoltariEmissaryTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts an activated ability on the stack")
    void activatingPutsAbilityOnStack() {
        addEmissaryReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
    }

    @Test
    @DisplayName("Resolving the ability grants shadow until end of turn")
    void resolvingGrantsShadow() {
        Permanent emissary = addEmissaryReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThat(gqs.hasKeyword(gd, emissary, Keyword.SHADOW)).isFalse();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, emissary, Keyword.SHADOW)).isTrue();
    }

    @Test
    @DisplayName("Granted shadow wears off at end of turn")
    void shadowWearsOffAtEndOfTurn() {
        Permanent emissary = addEmissaryReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, emissary, Keyword.SHADOW)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, emissary, Keyword.SHADOW)).isFalse();
    }

    @Test
    @DisplayName("Activating does not tap Soltari Emissary and works with summoning sickness")
    void activatingDoesNotTapAndIgnoresSummoningSickness() {
        Permanent emissary = new Permanent(new SoltariEmissary());
        gd.playerBattlefields.get(player1.getId()).add(emissary);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(emissary.isTapped()).isFalse();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without enough mana")
    void cannotActivateWithoutMana() {
        addEmissaryReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    private Permanent addEmissaryReady(Player player) {
        Permanent perm = new Permanent(new SoltariEmissary());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
