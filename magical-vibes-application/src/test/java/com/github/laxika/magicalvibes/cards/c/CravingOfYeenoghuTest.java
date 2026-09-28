package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CravingOfYeenoghu.class, GrizzlyBears.class})
class CravingOfYeenoghuTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +3/+2, haste, and must attack")
    void grantsStaticEffects() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new CravingOfYeenoghu()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Graveyard ability returns the Aura attached with a perpetual -1/-1 clause")
    void graveyardAbilityReturnsAttachedAndPerpetuallyShrinksEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.setGraveyard(player1, List.of(new CravingOfYeenoghu()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateGraveyardAbility(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.isAttached()
                        && permanent.getAttachedTo().equals(bears.getId())
                        && permanent.getCard().getName().equals("Craving of Yeenoghu"));
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HASTE)).isTrue();
        assertThat(als.getMustAttackRequirementCount(gd, bears)).isEqualTo(1);
    }

    @Test
    @DisplayName("Craving of Yeenoghu can target only a creature its controller controls")
    void requiresCreatureYouControl() {
        Permanent opponentBears = addCreatureReady(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new CravingOfYeenoghu()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature you control");
    }

}
