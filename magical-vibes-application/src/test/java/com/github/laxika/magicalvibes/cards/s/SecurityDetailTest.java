package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.f.Forest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SecurityDetailTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 white Soldier when no creatures are controlled")
    void createsSoldierWithoutControlledCreatures() {
        Permanent detail = harness.addToBattlefieldAndReturn(player1, new SecurityDetail());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(detail), null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Soldier")).singleElement().satisfies(token -> {
            assertThat(token.getCard().isToken()).isTrue();
            assertThat(token.getCard().getColor()).isEqualTo(CardColor.WHITE);
            assertThat(token.getCard().getSubtypes()).contains(CardSubtype.SOLDIER);
            assertThat(token.getEffectivePower()).isEqualTo(1);
            assertThat(token.getEffectiveToughness()).isEqualTo(1);
        });
    }

    @Test
    @DisplayName("Cannot be activated while its controller controls a creature")
    void cannotActivateWithControlledCreature() {
        Permanent detail = harness.addToBattlefieldAndReturn(player1, new SecurityDetail());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(detail), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no creatures");
        assertThat(findPermanents(player1, "Soldier")).isEmpty();
    }

    @Test
    @DisplayName("Cannot be activated more than once each turn")
    void onlyOnceEachTurn() {
        Permanent detail = harness.addToBattlefieldAndReturn(player1, new SecurityDetail());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(detail), null, null);
        harness.passBothPriorities();
        Permanent soldier = findPermanents(player1, "Soldier").getFirst();
        gd.playerBattlefields.get(player1.getId()).remove(soldier);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(
                player1, gd.playerBattlefields.get(player1.getId()).indexOf(detail), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("no more than 1 times each turn");
    }
}
