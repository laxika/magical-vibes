package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.b.BogRats;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YomijiWhoBarsTheWay;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NashiMoonsLegacy.class, BogRats.class, GrizzlyBears.class, YomijiWhoBarsTheWay.class})
class NashiMoonsLegacyTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking offers only a legendary or Rat card from your graveyard")
    void attackFiltersGraveyardTargets() {
        addCreatureReady(player1, new NashiMoonsLegacy());
        BogRats rat = new BogRats();
        YomijiWhoBarsTheWay legendary = new YomijiWhoBarsTheWay();
        GrizzlyBears invalid = new GrizzlyBears();
        BogRats opponentRat = new BogRats();
        harness.setGraveyard(player1, List.of(rat, legendary, invalid));
        harness.setGraveyard(player2, List.of(opponentRat));

        declareAttackers(List.of(0));

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(rat.getId(), legendary.getId());
    }

    @Test
    @DisplayName("Casts a copied Rat for its normal cost and makes a permanent copy a token")
    void castsRatCopyForNormalCost() {
        addCreatureReady(player1, new NashiMoonsLegacy());
        BogRats rat = new BogRats();
        harness.setGraveyard(player1, List.of(rat));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        declareAttackers(List.of(0));
        harness.handleMultipleCardsChosen(player1, List.of(rat.getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Bog Rats")
                        && permanent.getCard().isToken());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getId().equals(rat.getId()));
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLACK)).isZero();
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }
}
