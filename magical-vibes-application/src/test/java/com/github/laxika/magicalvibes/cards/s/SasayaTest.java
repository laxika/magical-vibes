package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.c.CityOfBrass;
import com.github.laxika.magicalvibes.cards.g.GnatMiser;
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

@CardUsed({Sasaya.class, SasayasEssence.class, Forest.class, GnatMiser.class, CityOfBrass.class})
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
        hand.add(new GnatMiser());
        harness.setHand(player1, hand);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(sasaya.isTransformed()).isFalse();
    }

    @Test
    @DisplayName("Adds green mana for each other Forest controlled")
    void addsManaForEachOtherLandWithSameName() {
        addTransformedSasaya();
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Forest());

        harness.tapPermanent(player1, 1);

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.GREEN)).isEqualTo(3);
    }

    @Test
    @DisplayName("Does not trigger when an opponent taps a land")
    void doesNotTriggerForOpponentsLand() {
        addTransformedSasaya();
        harness.addToBattlefield(player2, new Forest());

        harness.tapPermanent(player2, 0);

        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
        assertThat(gd.playerManaPools.get(player2.getId()).get(ManaColor.GREEN)).isEqualTo(1);
    }

    @Test
    @DisplayName("Adds the bonus when a same-name land produces a chosen color")
    void addsManaForSameNameLandProducingAnyColor() {
        addTransformedSasaya();
        harness.addToBattlefield(player1, new CityOfBrass());
        harness.addToBattlefield(player1, new CityOfBrass());

        harness.activateAbility(player1, 1, null, null);
        harness.handleListChoice(player1, "RED");
        resolveAllTriggers();

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(2);
    }

    private Permanent addSasaya() {
        return harness.addToBattlefieldAndReturn(player1, new Sasaya());
    }

    private Permanent addTransformedSasaya() {
        Permanent sasaya = addSasaya();
        sasaya.setTransformed(true);
        sasaya.setCard(sasaya.getOriginalCard().getBackFaceCard());
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
