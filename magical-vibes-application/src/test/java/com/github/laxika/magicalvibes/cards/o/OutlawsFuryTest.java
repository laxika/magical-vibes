package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.r.RogueSkycaptain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OutlawsFury.class, GrizzlyBears.class, RogueSkycaptain.class, Forest.class})
class OutlawsFuryTest extends BaseCardTest {

    @Test
    @DisplayName("Gives your creatures +2/+0 until end of turn and does not exile without an outlaw")
    void boostsOwnCreaturesWithoutOutlaw() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        castOutlawsFury();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, opposingCreature)).isEqualTo(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(topCard);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
    }

    @Test
    @DisplayName("With an outlaw, exiles the top card and allows playing it through your next turn")
    void boostsAndExilesTopCardWithOutlaw() {
        harness.addToBattlefield(player1, new RogueSkycaptain());
        Card topCard = new Forest();
        harness.setLibrary(player1, List.of(topCard));
        castOutlawsFury();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(topCard);
        assertThat(gd.exilePlayPermissions.get(topCard.getId())).isEqualTo(player1.getId());
    }

    private void castOutlawsFury() {
        harness.setHand(player1, List.of(new OutlawsFury()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
