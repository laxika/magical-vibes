package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PersonOfInterest.class, Shock.class})
class PersonOfInterestTest extends BaseCardTest {

    @Test
    void suspectsItselfAndCreatesDetectiveToken() {
        harness.setHand(player1, List.of(new PersonOfInterest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent person = findPermanent(player1, "Person of Interest");
        assertThat(person.isSuspected()).isTrue();
        assertThat(gqs.hasKeyword(gd, person, Keyword.MENACE)).isTrue();
        assertThat(bls.canBlock(gd, person)).isFalse();

        Permanent detective = findPermanent(player1, "Detective");
        assertThat(detective.getCard().isToken()).isTrue();
        assertThat(detective.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(detective.getCard().getPower()).isEqualTo(2);
        assertThat(detective.getCard().getToughness()).isEqualTo(2);
        assertThat(detective.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(detective.getCard().getSubtypes()).containsExactly(CardSubtype.DETECTIVE);
    }

    @Test
    void createsDetectiveEvenIfSourceDiesBeforeTriggerResolves() {
        harness.setHand(player1, List.of(new PersonOfInterest()));
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent person = findPermanent(player1, "Person of Interest");
        assertThat(person.isSuspected()).isFalse();
        assertThat(countPermanents(player1, "Detective")).isZero();

        harness.castAndResolveInstant(player2, 0, person.getId());
        harness.assertInGraveyard(player1, "Person of Interest");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Detective")).isEqualTo(1);
        Permanent detective = findPermanent(player1, "Detective");
        assertThat(detective.getCard().isToken()).isTrue();
        assertThat(detective.isSuspected()).isFalse();
        assertThat(bls.canBlock(gd, detective)).isTrue();
        assertThat(countPermanents(player2, "Detective")).isZero();
    }
}
