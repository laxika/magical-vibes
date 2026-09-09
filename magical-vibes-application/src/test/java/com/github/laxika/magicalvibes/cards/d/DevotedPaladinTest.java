package com.github.laxika.magicalvibes.cards.d;

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

@CardUsed({DevotedPaladin.class, GrizzlyBears.class})
class DevotedPaladinTest extends BaseCardTest {

    private void castPaladin() {
        harness.setHand(player1, List.of(new DevotedPaladin()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("ETB boosts and grants vigilance to creatures you control")
    void etbBoostsAndGrantsVigilance() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castPaladin();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(3);
        assertThat(bears.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isTrue();

        Permanent paladin = findPermanent(player1, "Devoted Paladin");
        assertThat(paladin.getEffectivePower()).isEqualTo(5);
        assertThat(paladin.getEffectiveToughness()).isEqualTo(5);
        assertThat(gqs.hasKeyword(gd, paladin, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("ETB does not affect an opponent's creatures")
    void doesNotAffectOpponentCreatures() {
        harness.addToBattlefield(player2, new GrizzlyBears());

        castPaladin();

        Permanent bears = findPermanent(player2, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }

    @Test
    @DisplayName("ETB boost and vigilance wear off at end of turn")
    void wearsOffAtEndOfTurn() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castPaladin();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(bears.getEffectivePower()).isEqualTo(2);
        assertThat(bears.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, bears, Keyword.VIGILANCE)).isFalse();
    }
}
