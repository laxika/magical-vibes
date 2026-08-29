package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(GruulNodorog.class)
class GruulNodorogTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        Permanent nodorog = addNodorogReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Gruul Nodorog");
        assertThat(entry.getTargetId()).isEqualTo(nodorog.getId());
    }

    @Test
    @DisplayName("Resolving the ability grants menace until end of turn")
    void resolvingAbilityGrantsMenace() {
        Permanent nodorog = addNodorogReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, nodorog, Keyword.MENACE)).isTrue();
    }

    @Test
    @DisplayName("Menace granted by the ability wears off at end of turn")
    void menaceWearsOffAtEndOfTurn() {
        Permanent nodorog = addNodorogReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.hasKeyword(gd, nodorog, Keyword.MENACE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, nodorog, Keyword.MENACE)).isFalse();
    }

    @Test
    @DisplayName("The ability requires one red mana")
    void requiresRedMana() {
        addNodorogReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("The ability does not tap Gruul Nodorog and can be activated while tapped")
    void doesNotTapAndWorksWhileTapped() {
        Permanent nodorog = addNodorogReady(player1);
        nodorog.tap();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        assertThat(nodorog.isTapped()).isTrue();
    }

    @Test
    @DisplayName("The ability fizzles if Gruul Nodorog leaves before resolution")
    void abilityFizzlesIfSourceLeaves() {
        addNodorogReady(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        gd.playerBattlefields.get(player1.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    private Permanent addNodorogReady(Player player) {
        Permanent perm = new Permanent(new GruulNodorog());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
