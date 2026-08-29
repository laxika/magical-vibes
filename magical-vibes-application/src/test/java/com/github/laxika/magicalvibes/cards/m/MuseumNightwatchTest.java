package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MuseumNightwatch.class, WrathOfGod.class})
class MuseumNightwatchTest extends BaseCardTest {

    @Test
    @DisplayName("When Museum Nightwatch dies, its controller creates a 2/2 white and blue Detective token")
    void deathTriggerCreatesDetectiveToken() {
        harness.addToBattlefield(player1, new MuseumNightwatch());
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent detective = findPermanent(player1, "Detective");
        assertThat(detective.getCard().getPower()).isEqualTo(2);
        assertThat(detective.getCard().getToughness()).isEqualTo(2);
        assertThat(detective.getCard().getColors()).containsExactlyInAnyOrder(CardColor.WHITE, CardColor.BLUE);
        assertThat(detective.getCard().getType()).isEqualTo(CardType.CREATURE);
        assertThat(detective.getCard().getSubtypes()).contains(CardSubtype.DETECTIVE);
        assertThat(detective.getCard().isToken()).isTrue();
    }

    @Test
    @DisplayName("Disguise casts Museum Nightwatch face down")
    void disguiseCastsFaceDown() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new MuseumNightwatch()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Museum Nightwatch").isFaceDown()).isTrue();
    }
}
