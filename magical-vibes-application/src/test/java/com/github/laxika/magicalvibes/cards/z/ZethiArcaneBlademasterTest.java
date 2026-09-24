package com.github.laxika.magicalvibes.cards.z;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.ThinkTwice;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ZethiArcaneBlademaster.class, ThinkTwice.class, GrizzlyBears.class})
class ZethiArcaneBlademasterTest extends BaseCardTest {

    @Test
    void multikickerExilesUpToTheNumberOfKicksAndAddsKickCounters() {
        Card first = new ThinkTwice();
        Card second = new ThinkTwice();
        harness.setGraveyard(player1, List.of(first, second));
        castZethi(List.of("{W/U}", "{W/U}"));

        harness.passBothPriorities();

        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.maxCount()).isEqualTo(2);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.exiledCardsWithKickCounters)
                .containsExactlyInAnyOrder(first.getId(), second.getId());
        assertThat(gd.getCardsExiledByPermanent(findPermanent(player1, "Zethi, Arcane Blademaster").getId()))
                .containsExactlyInAnyOrder(first, second);
    }

    @Test
    void attackOffersCopiesOnlyOfKickCounterCards() {
        Card kickedCard = new ThinkTwice();
        Card notKickedCard = new ThinkTwice();
        harness.setGraveyard(player1, List.of(kickedCard, notKickedCard));
        castZethi(List.of("{W/U}"));

        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(kickedCard.getId()));
        harness.passBothPriorities();

        Permanent zethi = findPermanent(player1, "Zethi, Arcane Blademaster");
        zethi.setSummoningSick(false);
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(zethi)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNotNull();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card instanceof GrizzlyBears);
    }

    @Test
    void withoutMultikickerTheEtbExilesNoCards() {
        Card instant = new ThinkTwice();
        harness.setGraveyard(player1, List.of(instant));
        harness.setHand(player1, List.of(new ZethiArcaneBlademaster()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void castZethi(List<String> repeatedAdditionalCosts) {
        harness.setHand(player1, List.of(new ZethiArcaneBlademaster()));
        harness.addMana(player1, ManaColor.WHITE, 1 + repeatedAdditionalCosts.size());
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        gs.playCard(gd, player1, 0, 0, null, null, List.of(), List.of(), false,
                null, null, null, null, null, false, null, null, null, null,
                repeatedAdditionalCosts, false);
    }
}
