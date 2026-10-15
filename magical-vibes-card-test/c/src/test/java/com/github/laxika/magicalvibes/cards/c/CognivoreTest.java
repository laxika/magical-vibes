package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AetherBurst;
import com.github.laxika.magicalvibes.cards.c.CulturalExchange;
import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.Werebear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Cognivore.class, AetherBurst.class, CulturalExchange.class, DuskImp.class, Plains.class, Werebear.class})
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

    @Test
    @DisplayName("Cognivore dies on resolution when there are no instant cards in graveyards")
    void diesOnResolutionWithNoInstants() {
        harness.castFromHand(player1, new Cognivore(), "{6}{U}{U}");
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Cognivore");
        harness.assertInGraveyard(player1, "Cognivore");
    }

    @Test
    @DisplayName("Cognivore counts opposing graveyard instants while in hand and graveyard")
    void characteristicPowerToughnessWorksOutsideBattlefield() {
        Cognivore card = new Cognivore();
        harness.setHand(player1, List.of(card));
        harness.setGraveyard(player2, createInstantCards(3));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(3);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(3);

        harness.setHand(player1, List.of());
        harness.setGraveyard(player1, List.of(card));
        harness.setGraveyard(player2, createInstantCards(1));

        assertThat(gqs.getEffectiveCardPower(gd, card)).isEqualTo(1);
        assertThat(gqs.getEffectiveCardToughness(gd, card)).isEqualTo(1);
    }

    @Test
    @DisplayName("An instant on the stack does not count until it resolves into the graveyard")
    void growsWhenInstantResolves() {
        harness.setGraveyard(player1, createInstantCards(1));
        Permanent cognivore = addCreatureReady(player1, new Cognivore());
        Permanent target = addCreatureReady(player2, new DuskImp());
        harness.setHand(player1, List.of(new AetherBurst()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castInstant(player1, 0, target.getId());

        assertThat(gqs.getEffectivePower(gd, cognivore)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, cognivore)).isEqualTo(1);

        harness.passBothPriorities();

        harness.assertInHand(player2, "Dusk Imp");
        assertThat(gqs.getEffectivePower(gd, cognivore)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, cognivore)).isEqualTo(2);
    }

    @Test
    @DisplayName("Cognivore cannot be blocked by a creature without flying or reach")
    void flyingPreventsGroundBlocker() {
        harness.setGraveyard(player1, createInstantCards(1));
        addCreatureReady(player1, new Cognivore());
        addCreatureReady(player2, new Werebear());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("Cognivore can be blocked by a flying creature")
    void flyingCreatureCanBlock() {
        harness.setGraveyard(player1, createInstantCards(1));
        addCreatureReady(player1, new Cognivore());
        Permanent blocker = addCreatureReady(player2, new DuskImp());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
