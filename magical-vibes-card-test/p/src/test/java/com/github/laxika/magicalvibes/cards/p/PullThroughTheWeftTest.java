package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HolyDay;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.t.TormodsCrypt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PullThroughTheWeft.class, GrizzlyBears.class, TormodsCrypt.class,
        Forest.class, Island.class, HolyDay.class})
class PullThroughTheWeftTest extends BaseCardTest {

    @Test
    void returnsUpToTwoNonlandPermanentsToHandAndTwoLandsTapped() {
        Card firstPermanent = new GrizzlyBears();
        Card secondPermanent = new TormodsCrypt();
        Card firstLand = new Forest();
        Card secondLand = new Island();
        Card instant = new HolyDay();
        Card spell = new PullThroughTheWeft();
        harness.setGraveyard(player1, List.of(firstPermanent, secondPermanent, firstLand, secondLand, instant));
        harness.setHand(player1, List.of(spell));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);
        choose(firstPermanent);
        choose(secondPermanent);
        choose(firstLand);
        choose(secondLand);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(firstPermanent.getId(), secondPermanent.getId())
                .doesNotContain(firstLand.getId(), secondLand.getId(), instant.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(permanent -> List.of(firstLand.getId(), secondLand.getId())
                        .contains(permanent.getCard().getId()))
                .extracting(Permanent::isTapped)
                .containsOnly(true);
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getId)
                .containsExactlyInAnyOrder(spell.getId(), instant.getId());
    }

    @Test
    void eachTargetGroupIsOptional() {
        Card permanent = new GrizzlyBears();
        Card land = new Forest();
        Card spell = new PullThroughTheWeft();
        harness.setGraveyard(player1, List.of(permanent, land));
        harness.setHand(player1, List.of(spell));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);
        chooseNothing();
        choose(permanent);
        chooseNothing();
        choose(land);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getId)
                .contains(permanent.getId())
                .doesNotContain(land.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(returned ->
                returned.getCard().getId().equals(land.getId()) && returned.isTapped());
    }

    @Test
    void targetGroupsOfferOnlyMatchingCards() {
        Card permanent = new GrizzlyBears();
        Card land = new Forest();
        Card instant = new HolyDay();
        harness.setGraveyard(player1, List.of(permanent, land, instant));
        harness.setHand(player1, List.of(new PullThroughTheWeft()));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);

        PendingInteraction.MultiGraveyardChoice firstChoice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(firstChoice.validCardIds()).contains(permanent.getId())
                .doesNotContain(land.getId(), instant.getId());
    }

    @Test
    void canResolveWithNoCardsInOwnGraveyard() {
        Card opposingLand = new Forest();
        Card spell = new PullThroughTheWeft();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opposingLand));
        harness.setHand(player1, List.of(spell));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(spell);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingLand);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
    }

    @Test
    void canDeclineEveryTargetEvenWhenCardsAreAvailable() {
        Card permanent = new GrizzlyBears();
        Card land = new Forest();
        Card spell = new PullThroughTheWeft();
        harness.setGraveyard(player1, List.of(permanent, land));
        harness.setHand(player1, List.of(spell));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);
        chooseNothing();
        chooseNothing();
        chooseNothing();
        chooseNothing();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(permanent, land, spell);
    }

    @Test
    void landTargetsAreDistinctAndOnlyFromOwnGraveyard() {
        Card firstLand = new Forest();
        Card secondLand = new Island();
        Card opposingLand = new Forest();
        harness.setGraveyard(player1, List.of(firstLand, secondLand));
        harness.setGraveyard(player2, List.of(opposingLand));
        harness.setHand(player1, List.of(new PullThroughTheWeft()));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);
        choose(firstLand);
        PendingInteraction.MultiGraveyardChoice secondChoice = gd.interaction
                .activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(secondChoice.validCardIds()).containsExactly(secondLand.getId());
        choose(secondLand);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactlyInAnyOrder(firstLand.getId(), secondLand.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingLand);
    }

    @Test
    void survivingLandTargetReturnsWhenOtherTargetLeavesGraveyard() {
        Card removedLand = new Forest();
        Card survivingLand = new Island();
        harness.setGraveyard(player1, List.of(removedLand, survivingLand));
        harness.setHand(player1, List.of(new PullThroughTheWeft()));
        addManaForSpell();

        harness.castSorcery(player1, 0, 0);
        choose(removedLand);
        choose(survivingLand);
        harness.setGraveyard(player1, List.of(survivingLand));
        harness.setExile(player1, List.of(removedLand));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .extracting(permanent -> permanent.getCard().getId())
                .containsExactly(survivingLand.getId());
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .allMatch(Permanent::isTapped);
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(removedLand);
    }

    private void choose(Card card) {
        harness.handleMultipleCardsChosen(player1, List.of(card.getId()));
    }

    private void chooseNothing() {
        harness.handleMultipleCardsChosen(player1, List.of());
    }

    private void addManaForSpell() {
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
    }
}
