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
import com.github.laxika.magicalvibes.cards.f.Forest;
import org.junit.jupiter.api.DisplayName;




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

@CardUsed({WebweaverChangeling.class, GrizzlyBears.class, Forest.class})
class Mh1WebweaverChangelingTest extends BaseCardTest {

    @Test
    @DisplayName("ETB gains 5 life with three creature cards in your graveyard")
    void etbGainsLifeWithThreeCreatureCards() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        castWebweaverChangeling();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(25);
    }

    @Test
    @DisplayName("ETB does not count noncreature cards or cards in an opponent's graveyard")
    void etbDoesNotCountNoncreatureOrOpponentCards() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new Forest()));
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        castWebweaverChangeling();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("ETB condition is checked again when the trigger resolves")
    void etbConditionMustStillBeMetOnResolution() {
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new WebweaverChangeling()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    private void castWebweaverChangeling() {
        harness.setHand(player1, List.of(new WebweaverChangeling()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
