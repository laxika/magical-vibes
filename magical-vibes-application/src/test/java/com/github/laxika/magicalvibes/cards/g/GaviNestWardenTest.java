package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.Censor;
import com.github.laxika.magicalvibes.cards.c.CounselOfTheSoratami;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GaviNestWarden.class, Censor.class, GrizzlyBears.class, CounselOfTheSoratami.class})
class GaviNestWardenTest extends BaseCardTest {

    @Test
    @DisplayName("The first card cycled each turn costs no mana, but the second does")
    void firstCardCycledEachTurnIsFreeOnlyOnce() {
        harness.addToBattlefield(player1, new GaviNestWarden());
        Censor first = new Censor();
        Censor second = new Censor();
        harness.setHand(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));

        harness.activateHandAbility(player1, 0, null);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(card -> card.getId())
                .contains(first.getId());
        assertThatThrownBy(() -> harness.activateHandAbility(player1, 0, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Drawing the second card each turn creates a 2/2 red and white Dinosaur Cat")
    void secondDrawCreatesDinosaurCat() {
        harness.addToBattlefield(player1, new GaviNestWarden());
        harness.setHand(player1, List.of(new CounselOfTheSoratami()));
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent token = findPermanent(player1, "Dinosaur Cat");
        assertThat(token.getCard().getColors()).containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(token.getCard().getSubtypes())
                .containsExactlyInAnyOrder(CardSubtype.DINOSAUR, CardSubtype.CAT);
        assertThat(gqs.getEffectivePower(gd, token)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, token)).isEqualTo(2);
    }
}
