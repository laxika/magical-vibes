package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AvenTrooper;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SetonsScout.class, AvenTrooper.class})
class SetonsScoutTest extends BaseCardTest {

    @Test
    @DisplayName("Can block a creature with flying")
    void canBlockFlyingCreature() {
        addCreatureReady(player1, new AvenTrooper());
        Permanent scout = addCreatureReady(player2, new SetonsScout());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(scout.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Remains 2/1 with fewer than seven cards in its controller's graveyard")
    void remainsBaseSizeBelowThreshold() {
        harness.setGraveyard(player1, graveyardCards(6));
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new SetonsScout());

        assertThat(gqs.getEffectivePower(gd, scout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scout)).isEqualTo(1);
    }

    @Test
    @DisplayName("Gets +2/+2 at seven cards in its controller's graveyard")
    void getsBoostAtThreshold() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new SetonsScout());

        assertThat(gqs.getEffectivePower(gd, scout)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, scout)).isEqualTo(3);
    }

    @Test
    @DisplayName("Counts only its controller's graveyard")
    void opponentGraveyardDoesNotCount() {
        harness.setGraveyard(player2, graveyardCards(7));
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new SetonsScout());

        assertThat(gqs.getEffectivePower(gd, scout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scout)).isEqualTo(1);
    }

    @Test
    @DisplayName("Loses the boost when its controller's graveyard drops below seven cards")
    void losesBoostBelowThreshold() {
        harness.setGraveyard(player1, graveyardCards(7));
        Permanent scout = harness.addToBattlefieldAndReturn(player1, new SetonsScout());

        assertThat(gqs.getEffectivePower(gd, scout)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, scout)).isEqualTo(3);

        gd.playerGraveyards.get(player1.getId()).removeLast();

        assertThat(gqs.getEffectivePower(gd, scout)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, scout)).isEqualTo(1);
    }

    private List<Card> graveyardCards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new SetonsScout());
        }
        return cards;
    }
}
