package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AetherBurst;
import com.github.laxika.magicalvibes.cards.c.CulturalExchange;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Cognivore.class, AetherBurst.class, CulturalExchange.class, DuskImp.class, Plains.class})
class CognivoreTest extends BaseCardTest {

    @Test
    @DisplayName("Cognivore is 0/0 with no instant cards in any graveyard")
    void isZeroZeroWithEmptyGraveyards() {
        Permanent perm = addCreatureReady(player1, new Cognivore());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Cognivore P/T equals the number of instant cards in all graveyards")
    void ptEqualsInstantCountInAllGraveyards() {
        Permanent perm = addCreatureReady(player1, new Cognivore());
        harness.setGraveyard(player1, createInstantCards(2));
        harness.setGraveyard(player2, createInstantCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Cognivore counts only instant cards")
    void onlyCountsInstantCards() {
        Permanent perm = addCreatureReady(player1, new Cognivore());

        List<Card> graveyard = new ArrayList<>(createInstantCards(2));
        graveyard.add(new Plains());
        graveyard.add(new DuskImp());
        graveyard.add(new CulturalExchange());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cognivore P/T updates when an instant enters a graveyard")
    void ptUpdatesWhenInstantAdded() {
        Permanent perm = addCreatureReady(player1, new Cognivore());
        harness.setGraveyard(player1, createInstantCards(1));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new AetherBurst());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cognivore P/T updates when an instant leaves a graveyard")
    void ptUpdatesWhenInstantRemoved() {
        Permanent perm = addCreatureReady(player1, new Cognivore());
        harness.setGraveyard(player1, createInstantCards(2));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);
    }

    private List<Card> createInstantCards(int count) {
        List<Card> instants = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            instants.add(new AetherBurst());
        }
        return instants;
    }
}
