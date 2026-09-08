package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BodyDouble.class, GrizzlyBears.class})
class BodyDoubleTest extends BaseCardTest {

    @Test
    void entersAsACopyOfACreatureCardInAnyGraveyard() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        castBodyDouble();

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent bodyDouble = findBodyDouble();
        assertThat(bodyDouble).isNotNull();
        assertThat(bodyDouble.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(bodyDouble.getCard().getPower()).isEqualTo(2);
        assertThat(bodyDouble.getCard().getToughness()).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(bears);
    }

    @Test
    void mayDeclineToCopy() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        castBodyDouble();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Body Double");
    }

    private void castBodyDouble() {
        harness.setHand(player1, List.of(new BodyDouble()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.castCreature(player1, 0);
    }

    private Permanent findBodyDouble() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof BodyDouble)
                .findFirst()
                .orElse(null);
    }
}
