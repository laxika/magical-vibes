package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MightOfOaks;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({LongRiversPull.class, GrizzlyBears.class, MightOfOaks.class})
class LongRiversPullTest extends BaseCardTest {

    @Test
    void withoutGiftCountersCreatureSpell() {
        GrizzlyBears bears = new GrizzlyBears();
        harness.setHand(player1, List.of(bears));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.setHand(player2, List.of(new LongRiversPull()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstantWithGift(player2, 0, bears.getId(), false);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    void withoutGiftCannotTargetNoncreatureSpell() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new LongRiversPull()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passPriority(player1);

        assertThatThrownBy(() -> harness.castInstantWithGift(player2, 0, might.getId(), false))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("creature spell");
    }

    @Test
    void promisedGiftCountersAnySpellAndDrawsCardForOpponent() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        MightOfOaks might = new MightOfOaks();
        harness.setHand(player1, List.of(might));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.setHand(player2, List.of(new LongRiversPull()));
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player1, 0, bears.getId());
        harness.passPriority(player1);
        int handSizeBeforeGift = gd.playerHands.get(player1.getId()).size();
        harness.castInstantWithGift(player2, 0, might.getId(), true);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Might of Oaks");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBeforeGift + 1);
    }
}
