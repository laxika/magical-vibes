package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WebweaverChangeling.class, GrizzlyBears.class, Plains.class})
class WebweaverChangelingTest extends BaseCardTest {

    @Test
    void gainsFiveLifeWithThreeCreatureCardsInGraveyard() {
        harness.setLife(player1, 10);
        harness.setGraveyard(player1, creatureCards(3));

        castWebweaverChangeling();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(15);
    }

    @Test
    void doesNotGainLifeWithFewerThanThreeCreatureCards() {
        harness.setLife(player1, 10);
        List<Card> graveyard = creatureCards(2);
        graveyard.add(new Plains());
        harness.setGraveyard(player1, graveyard);

        castWebweaverChangeling();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void rechecksThresholdWhenEtbTriggerResolves() {
        harness.setLife(player1, 10);
        harness.setGraveyard(player1, creatureCards(3));

        castWebweaverChangeling();
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        gd.playerGraveyards.get(player1.getId()).removeFirst();

        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(10);
    }

    private List<Card> creatureCards(int count) {
        List<Card> creatures = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            creatures.add(new GrizzlyBears());
        }
        return creatures;
    }

    private void castWebweaverChangeling() {
        harness.setHand(player1, List.of(new WebweaverChangeling()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
    }
}
