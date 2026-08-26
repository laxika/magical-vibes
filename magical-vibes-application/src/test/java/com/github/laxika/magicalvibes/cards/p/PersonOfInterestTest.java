package com.github.laxika.magicalvibes.cards.p;

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

@CardUsed(PersonOfInterest.class)
class PersonOfInterestTest extends BaseCardTest {

    @Test
    void suspectsItselfAndCreatesDetectiveToken() {
        harness.setHand(player1, List.of(new PersonOfInterest()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent person = findPermanent(player1, "Person of Interest");
        assertThat(person.isSuspected()).isTrue();
        assertThat(gqs.hasKeyword(gd, person, Keyword.MENACE)).isTrue();
        assertThat(bls.canBlock(gd, person)).isFalse();

        Permanent detective = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Detective"))
                .findFirst()
                .orElseThrow(() -> new AssertionError("Detective token not found"));
        assertThat(detective.getCard().hasType(CardType.CREATURE)).isTrue();
        assertThat(detective.getCard().getPower()).isEqualTo(2);
        assertThat(detective.getCard().getToughness()).isEqualTo(2);
        assertThat(detective.getCard().getColors())
                .containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(detective.getCard().getSubtypes()).containsExactly(CardSubtype.DETECTIVE);
    }
}
