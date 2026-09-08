package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.w.WrathOfGod;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ArashinSovereign.class, Plains.class, WrathOfGod.class})
class ArashinSovereignTest extends BaseCardTest {

    @Test
    void mayBePutOnTopOfItsOwnersLibraryWhenItDies() {
        Card topCard = new Plains();
        harness.setLibrary(player1, List.of(topCard));
        Card sovereignCard = addSovereign();

        destroySovereign();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Top");

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(sovereignCard.getId(), topCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(sovereignCard.getId()));
    }

    @Test
    void mayBePutOnBottomOfItsOwnersLibraryWhenItDies() {
        Card topCard = new Plains();
        Card bottomCard = new Plains();
        harness.setLibrary(player1, List.of(topCard, bottomCard));
        Card sovereignCard = addSovereign();

        destroySovereign();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleListChoice(player1, "Bottom");

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(topCard.getId(), bottomCard.getId(), sovereignCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).noneMatch(card -> card.getId().equals(sovereignCard.getId()));
    }

    @Test
    void mayBeDeclinedAndRemainInItsOwnersGraveyard() {
        Card topCard = new Plains();
        harness.setLibrary(player1, List.of(topCard));
        Card sovereignCard = addSovereign();

        destroySovereign();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getId)
                .containsExactly(topCard.getId());
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .contains(sovereignCard.getId());
    }

    private Card addSovereign() {
        Permanent sovereign = harness.addToBattlefieldAndReturn(player1, new ArashinSovereign());
        return sovereign.getCard();
    }

    private void destroySovereign() {
        harness.setHand(player1, List.of(new WrathOfGod()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        harness.getGameService().playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
    }
}
