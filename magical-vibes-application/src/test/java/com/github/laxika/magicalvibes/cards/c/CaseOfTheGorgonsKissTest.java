package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({CaseOfTheGorgonsKiss.class, GiantSpider.class, Shock.class})
class CaseOfTheGorgonsKissTest extends BaseCardTest {

    @Test
    @DisplayName("Destroys up to one creature that was dealt damage this turn")
    void destroysDamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new Shock(), new CaseOfTheGorgonsKiss()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();
        harness.castEnchantment(player1, 0, List.of(target.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Cannot target an undamaged creature")
    void cannotTargetUndamagedCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        harness.setHand(player1, List.of(new CaseOfTheGorgonsKiss()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, List.of(target.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("dealt damage this turn");
    }

    @Test
    @DisplayName("Solves after three creature cards are put into graveyards and becomes a 4/4 Gorgon")
    void solvesAfterThreeCreatureCardsEnterGraveyards() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheGorgonsKiss());
        Permanent first = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent second = harness.addToBattlefieldAndReturn(player2, new GiantSpider());
        Permanent third = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        harness.inMutationScope(() -> {
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, first);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, second);
            harness.getPermanentRemovalService().removePermanentToGraveyard(gd, third);
        });
        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isTrue();
        assertThat(gqs.isCreature(gd, casePermanent)).isTrue();
        assertThat(gqs.getEffectivePower(gd, casePermanent)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, casePermanent)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, casePermanent, Keyword.DEATHTOUCH)).isTrue();
        assertThat(gqs.hasKeyword(gd, casePermanent, Keyword.LIFELINK)).isTrue();
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
