package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AquitectsDefensesTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Aquitect's Defenses boosts the enchanted creature and grants hexproof")
    void resolvingBoostsAndGrantsHexproof() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAquitectsDefenses(bears);

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
    }

    @Test
    @DisplayName("The entry hexproof grant expires at end of turn while the Aura's boost remains")
    void entryHexproofExpiresAtEndOfTurn() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        castAquitectsDefenses(bears);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isFalse();
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
    }

    @Test
    @DisplayName("Aquitect's Defenses can enchant only a creature its controller controls")
    void cannotEnchantOpponentCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new AquitectsDefenses()));
        harness.addMana(player1, ManaColor.BLUE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castAquitectsDefenses(Permanent target) {
        harness.setHand(player1, List.of(new AquitectsDefenses()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
