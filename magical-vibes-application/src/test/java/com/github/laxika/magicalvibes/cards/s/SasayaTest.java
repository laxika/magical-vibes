package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SasayaTest extends BaseCardTest {

    @Test
    @DisplayName("Reveals the hand as a cost and flips with seven land cards")
    void revealsHandAndFlipsWithSevenLands() {
        Permanent sasaya = addSasaya();
        harness.setHand(player1, lands(7));

        harness.activateAbility(player1, 0, null, null);

        assertThat(gameLogContains("reveals their hand")).isTrue();
        assertThat(sasaya.isTransformed()).isFalse();

        harness.passBothPriorities();

        assertThat(sasaya.isTransformed()).isTrue();
    }

    @Test
    @DisplayName("Does not flip when fewer than seven hand cards are lands")
    void doesNotFlipWithFewerThanSevenLands() {
        Permanent sasaya = addSasaya();
        List<Card> hand = lands(6);
        hand.add(new GrizzlyBears());
        harness.setHand(player1, hand);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sasaya.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Adds green mana for each other Forest controlled")
    void addsManaForEachOtherLandWithSameName() {
        Permanent sasaya = addSasaya();
        sasaya.setTransformed(true);
        sasaya.setCard(sasaya.getOriginalCard().getBackFaceCard());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    private Permanent addSasaya() {
        Permanent sasaya = new Permanent(new Sasaya());
        sasaya.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(sasaya);
        return sasaya;
    }

    private List<Card> lands(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new Forest());
        }
        return cards;
    }
}
