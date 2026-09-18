package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.k.KrosanAvenger;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Magnivore.class, MindBurst.class, KrosanAvenger.class, Plains.class})
class MagnivoreTest extends BaseCardTest {

    @Test
    @DisplayName("Magnivore is 0/0 with no sorcery cards in any graveyard")
    void isZeroZeroWithEmptyGraveyards() {
        Permanent perm = addCreatureReady(player1, new Magnivore());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(0);
    }

    @Test
    @DisplayName("Magnivore P/T equals number of sorcery cards in controller's graveyard")
    void ptEqualsSorceryCountInOwnGraveyard() {
        Permanent perm = addCreatureReady(player1, new Magnivore());
        harness.setGraveyard(player1, createSorceryCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(3);
    }

    @Test
    @DisplayName("Magnivore P/T counts sorcery cards in ALL graveyards")
    void ptCountsAllGraveyards() {
        Permanent perm = addCreatureReady(player1, new Magnivore());
        harness.setGraveyard(player1, createSorceryCards(2));
        harness.setGraveyard(player2, createSorceryCards(3));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(5);
    }

    @Test
    @DisplayName("Magnivore only counts sorcery cards, not other card types")
    void onlyCountsSorceryCards() {
        Permanent perm = addCreatureReady(player1, new Magnivore());

        List<Card> graveyard = new ArrayList<>();
        graveyard.addAll(createSorceryCards(2));
        graveyard.add(new Plains());
        graveyard.add(new KrosanAvenger());
        harness.setGraveyard(player1, graveyard);

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Magnivore P/T updates when a sorcery is added to a graveyard")
    void ptUpdatesWhenSorceryAdded() {
        Permanent perm = addCreatureReady(player1, new Magnivore());
        harness.setGraveyard(player1, createSorceryCards(1));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);

        gd.playerGraveyards.get(player1.getId()).add(new MindBurst());

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);
    }

    @Test
    @DisplayName("Magnivore P/T updates when a sorcery leaves a graveyard")
    void ptUpdatesWhenSorceryRemoved() {
        Permanent perm = addCreatureReady(player1, new Magnivore());
        harness.setGraveyard(player1, createSorceryCards(2));

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(2);

        gd.playerGraveyards.get(player1.getId()).removeFirst();

        assertThat(gqs.getEffectivePower(gd, perm)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, perm)).isEqualTo(1);
    }

    @Test
    @DisplayName("Magnivore can attack the turn it enters because it has haste")
    void canAttackTheTurnItEnters() {
        harness.setGraveyard(player1, createSorceryCards(1));
        harness.setHand(player1, List.of(new Magnivore()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Magnivore");
        declareAttackers(List.of(0));

        assertThat(perm.isTapped()).isTrue();
    }

    private List<Card> createSorceryCards(int count) {
        List<Card> sorceries = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            sorceries.add(new MindBurst());
        }
        return sorceries;
    }
}
