package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TimeLordRegeneration.class, TheFourthDoctor.class, TheTenthDoctor.class,
        DoomBlade.class, GrizzlyBears.class})
class TimeLordRegenerationTest extends BaseCardTest {

    @Test
    void timeLordReturnsAnotherTimeLordFromLibraryWhenItDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new TheFourthDoctor());
        Card nonmatchingCard = new GrizzlyBears();
        Card foundCard = new TheTenthDoctor();
        harness.setLibrary(player1, List.of(nonmatchingCard, foundCard));

        harness.setHand(player1, List.of(new TimeLordRegeneration()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castInstant(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.setHand(player2, List.of(new DoomBlade()));
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == foundCard);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(nonmatchingCard);
    }

    @Test
    void cannotTargetNonTimeLordYouControl() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new TimeLordRegeneration()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.castInstant(player1, 0, bears.getId()))
                .hasMessageContaining("Time Lord");
    }
}
