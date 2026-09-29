package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.Clue;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FiveHundredYearDiary.class, Clue.class, GrizzlyBears.class})
class FiveHundredYearDiaryTest extends BaseCardTest {

    @Test
    void entersTapped() {
        harness.setHand(player1, List.of(new FiveHundredYearDiary()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castArtifact(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()).getFirst().isTapped()).isTrue();
    }

    @Test
    void addsBlueManaForEachClueControlled() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new FiveHundredYearDiary());
        diary.untap();
        harness.addToBattlefield(player1, new Clue());
        harness.addToBattlefield(player1, new Clue());

        harness.activateAbility(player1, 0, null, null);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(3);
        assertThat(diary.isTapped()).isTrue();
    }

    @Test
    void sacrificesAndDrawsACard() {
        Permanent diary = harness.addToBattlefieldAndReturn(player1, new FiveHundredYearDiary());
        diary.untap();
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(diary);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(diary.getCard());
        assertThat(gd.playerHands.get(player1.getId())).contains(drawnCard);
    }
}
