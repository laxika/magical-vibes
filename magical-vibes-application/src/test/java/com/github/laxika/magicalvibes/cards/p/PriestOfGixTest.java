package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(PriestOfGix.class)
class PriestOfGixTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Priest of Gix puts it on the stack as a creature spell")
    void castingPutsOnStack() {
        castPriestOfGix();

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving Priest of Gix puts it on the battlefield with an ETB trigger")
    void resolvingPutsItOnBattlefieldWithEtbOnStack() {
        castPriestOfGix();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
    }

    @Test
    @DisplayName("Priest of Gix's ETB trigger adds three black mana to its controller's pool")
    void etbAddsThreeBlackMana() {
        castPriestOfGix();
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.BLACK)).isEqualTo(0);
    }

    private void castPriestOfGix() {
        harness.castFromHand(player1, new PriestOfGix(), "{2}{B}");
    }
}
