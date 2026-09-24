package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(YavimayaIconoclast.class)
class YavimayaIconoclastTest extends BaseCardTest {

    @Test
    void castWithoutKickerDoesNotApplyEtbBonus() {
        harness.setHand(player1, List.of(new YavimayaIconoclast()));
        addBaseMana();

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent iconoclast = findPermanent();
        assertThat(iconoclast.getEffectivePower()).isEqualTo(3);
        assertThat(iconoclast.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, iconoclast, Keyword.HASTE)).isFalse();
    }

    @Test
    void kickedCastGetsBonusAndHasteUntilEndOfTurn() {
        harness.setHand(player1, List.of(new YavimayaIconoclast()));
        addBaseMana();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castKickedCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent iconoclast = findPermanent();
        assertThat(iconoclast.getEffectivePower()).isEqualTo(4);
        assertThat(iconoclast.getEffectiveToughness()).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, iconoclast, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(iconoclast.getEffectivePower()).isEqualTo(3);
        assertThat(iconoclast.getEffectiveToughness()).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, iconoclast, Keyword.HASTE)).isFalse();
    }

    private void addBaseMana() {
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
    }

    private Permanent findPermanent() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Yavimaya Iconoclast"))
                .findFirst()
                .orElseThrow();
    }
}
