package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CennsHeir;
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

class GallantFowlknightTest extends BaseCardTest {

    @Test
    @DisplayName("Boosts all creatures you control and gives Kithkin first strike")
    void boostsOwnCreaturesAndGrantsKithkinFirstStrike() {
        Permanent kithkin = harness.addToBattlefieldAndReturn(player1, new CennsHeir());
        Permanent nonKithkin = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new GallantFowlknight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, GallantFowlknight.class);
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, kithkin)).isEqualTo(2);
        assertThat(gqs.getEffectivePower(gd, nonKithkin)).isEqualTo(3);
        assertThat(gqs.getEffectivePower(gd, opponentCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, kithkin, Keyword.FIRST_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonKithkin, Keyword.FIRST_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, opponentCreature, Keyword.FIRST_STRIKE)).isFalse();
    }

    @Test
    @DisplayName("ETB boost and first strike last until end of turn")
    void effectsWearOffAtEndOfTurn() {
        harness.setHand(player1, List.of(new GallantFowlknight()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent source = findPermanent(player1, GallantFowlknight.class);
        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, source)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, source, Keyword.FIRST_STRIKE)).isFalse();
    }

    private Permanent findPermanent(com.github.laxika.magicalvibes.model.Player player,
                                    Class<? extends com.github.laxika.magicalvibes.model.Card> cardClass) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> cardClass.isInstance(permanent.getCard()))
                .findFirst()
                .orElseThrow();
    }
}
