package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BagEndBanquet;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TastefulOffering.class, BagEndBanquet.class, GrizzlyBears.class})
class TastefulOfferingTest extends BaseCardTest {

    @Test
    void createsFoodAndSeeksForTheFirstTwoSacrificesOnly() {
        GrizzlyBears first = new GrizzlyBears();
        GrizzlyBears second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        harness.setHand(player1, List.of(new BagEndBanquet()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.castArtifact(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new TastefulOffering()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Food")).hasSize(4);

        sacrificeFoodAndResolve();
        sacrificeFoodAndResolve();
        sacrificeFoodAndResolve();

        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
    }

    private void sacrificeFoodAndResolve() {
        Permanent food = findPermanents(player1, "Food").getFirst();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, battlefieldIndex(food), 0, null);
        harness.passBothPriorities();
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
