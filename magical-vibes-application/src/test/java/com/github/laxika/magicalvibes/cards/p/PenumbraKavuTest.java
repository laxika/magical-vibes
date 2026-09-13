package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.v.Vindicate;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PenumbraKavu.class, Vindicate.class})
class PenumbraKavuTest extends BaseCardTest {

    @Test
    @DisplayName("When Penumbra Kavu dies, it creates a 3/3 black Kavu token")
    void deathCreatesBlackKavuToken() {
        Permanent kavu = harness.addToBattlefieldAndReturn(player1, new PenumbraKavu());
        harness.setHand(player1, List.of(new Vindicate()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, kavu.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Penumbra Kavu");
        Permanent token = findPermanent(player1, "Kavu");
        assertThat(token.getCard().isToken()).isTrue();
        assertThat(token.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(token.getEffectivePower()).isEqualTo(3);
        assertThat(token.getEffectiveToughness()).isEqualTo(3);
        assertThat(token.getCard().getColor()).isEqualTo(CardColor.BLACK);
        assertThat(token.getCard().getSubtypes()).contains(CardSubtype.KAVU);
    }

    @Test
    @DisplayName("Each Penumbra Kavu creates a token when multiple Kavus die together")
    void eachKavuCreatesATokenWhenTheyDieTogether() {
        Permanent firstKavu = harness.addToBattlefieldAndReturn(player1, new PenumbraKavu());
        Permanent secondKavu = harness.addToBattlefieldAndReturn(player1, new PenumbraKavu());
        firstKavu.setMarkedDamage(3);
        secondKavu.setMarkedDamage(3);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Kavu")).hasSize(2);
    }
}
