package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EssenceOfAntiquity.class, GrizzlyBears.class})
class EssenceOfAntiquityTest extends BaseCardTest {

    @Test
    @DisplayName("Turning face up gives your creatures hexproof and untaps them")
    void turningFaceUpGrantsHexproofAndUntapsControlledCreatures() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingBears = addCreatureReady(player2, new GrizzlyBears());
        bears.tap();
        opposingBears.tap();

        harness.setHand(player1, List.of(new EssenceOfAntiquity()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent essence = findPermanent(player1, "Essence of Antiquity");
        essence.tap();
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(essence));
        harness.passBothPriorities();

        assertThat(essence.isFaceDown()).isFalse();
        assertThat(gqs.hasKeyword(gd, essence, Keyword.HEXPROOF)).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.HEXPROOF)).isTrue();
        assertThat(essence.isTapped()).isFalse();
        assertThat(bears.isTapped()).isFalse();
        assertThat(opposingBears.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingBears, Keyword.HEXPROOF)).isFalse();
    }
}
