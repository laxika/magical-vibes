package com.github.laxika.magicalvibes.cards.l;

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

@CardUsed({LoomingSpires.class, GrizzlyBears.class})
class LoomingSpiresTest extends BaseCardTest {

    @Test
    @DisplayName("Enters tapped and grants a creature +1/+1 and first strike until end of turn")
    void entersTappedAndBoostsTargetCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new LoomingSpires()));

        harness.playLand(player1, 0);
        harness.handlePermanentChosen(player1, target.getId());

        Permanent spires = gd.playerBattlefields.get(player1.getId()).getLast();
        assertThat(spires.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, target)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, target)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, target, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("Tapping adds red mana")
    void tapsForRedMana() {
        Permanent spires = harness.addToBattlefieldAndReturn(player1, new LoomingSpires());
        spires.untap();

        harness.activateAbility(player1, 0, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
        assertThat(spires.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetLand() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new LoomingSpires());
        harness.setHand(player1, List.of(new LoomingSpires()));

        assertThatThrownBy(() -> harness.getGameService().playCard(gd, player1, 0, 0, land.getId(), null))
                .isInstanceOf(IllegalStateException.class);
    }
}
