package com.github.laxika.magicalvibes.cards.f;

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

class FemerefKnightTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving the ability grants vigilance until end of turn")
    void resolvingGrantsVigilance() {
        Permanent knight = addKnightReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Granted vigilance wears off at end of turn")
    void vigilanceWearsOff() {
        Permanent knight = addKnightReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("Cannot activate the ability without white mana")
    void cannotActivateWithoutMana() {
        addKnightReady(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Ability can be activated repeatedly and does not tap the knight")
    void activationDoesNotTap() {
        Permanent knight = addKnightReady(player1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(knight.isTapped()).isFalse();
        assertThat(gqs.hasKeyword(gd, knight, Keyword.VIGILANCE)).isTrue();
    }

    private Permanent addKnightReady(Player player) {
        Permanent perm = new Permanent(new FemerefKnight());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
