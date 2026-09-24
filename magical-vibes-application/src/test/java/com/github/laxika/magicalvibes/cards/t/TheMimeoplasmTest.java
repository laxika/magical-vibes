package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TheMimeoplasm.class, GrizzlyBears.class, HillGiant.class, AirElemental.class})
class TheMimeoplasmTest extends BaseCardTest {

    @Test
    void exilesTwoCreatureCardsCopiesOneAndUsesOtherPowerForCounters() {
        Card bears = new GrizzlyBears();
        Card elemental = new AirElemental();
        harness.setGraveyard(player1, List.of(bears));
        harness.setGraveyard(player2, List.of(elemental));
        castMimeoplasm();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        PendingInteraction.MultiGraveyardChoice pairChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(pairChoice).isNotNull();
        assertThat(pairChoice.minCount()).isEqualTo(2);
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId(), elemental.getId()));

        PendingInteraction.MultiGraveyardChoice copyChoice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(copyChoice).isNotNull();
        assertThat(copyChoice.validCardIds()).containsExactlyInAnyOrder(bears.getId(), elemental.getId());
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        Permanent mimeoplasm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getOriginalCard() instanceof TheMimeoplasm)
                .findFirst()
                .orElse(null);
        assertThat(mimeoplasm).isNotNull();
        assertThat(mimeoplasm.getCard().getName()).isEqualTo("Grizzly Bears");
        assertThat(mimeoplasm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(mimeoplasm.getEffectivePower()).isEqualTo(6);
        assertThat(mimeoplasm.getEffectiveToughness()).isEqualTo(6);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(bears);
        assertThat(gd.getPlayerExiledCards(player2.getId())).contains(elemental);
    }

    @Test
    void mayDeclineToExileCardsAndCopy() {
        Card bears = new GrizzlyBears();
        Card elemental = new AirElemental();
        harness.setGraveyard(player1, List.of(bears));
        harness.setGraveyard(player2, List.of(elemental));
        castMimeoplasm();

        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "The Mimeoplasm");
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(bears);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(elemental);
    }

    private void castMimeoplasm() {
        harness.setHand(player1, List.of(new TheMimeoplasm()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
    }
}
