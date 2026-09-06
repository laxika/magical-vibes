package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KnowledgeSeeker.class, GrizzlyBears.class, Shock.class})
class KnowledgeSeekerTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a +1/+1 counter on itself when its controller draws their second card each turn")
    void triggersOnControllerSecondCardDraw() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new KnowledgeSeeker());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        drawAndResolveTrigger(player1);
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();

        drawAndResolveTrigger(player1);
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        drawAndResolveTrigger(player1);
        assertThat(seeker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates a Clue when it dies")
    void createsClueWhenItDies() {
        Permanent seeker = harness.addToBattlefieldAndReturn(player1, new KnowledgeSeeker());
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, seeker.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Knowledge Seeker");
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    private void drawAndResolveTrigger(Player player) {
        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
    }
}
