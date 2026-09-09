package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static com.github.laxika.magicalvibes.model.Keyword.DOUBLE_STRIKE;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TicketBoothTunnelOfHate.class, GrizzlyBears.class, Forest.class})
class TicketBoothTunnelOfHateTest extends BaseCardTest {

    @Test
    void ticketBoothManifestsDreadWhenItsDoorIsUnlocked() {
        Card manifestedCard = new GrizzlyBears();
        Card graveyardCard = new Forest();
        harness.setHand(player1, List.of(new TicketBoothTunnelOfHate()));
        harness.setLibrary(player1, List.of(manifestedCard, graveyardCard));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castModalSorcery(player1, 0, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.allCards()).containsExactly(manifestedCard, graveyardCard);

        harness.handleMultipleCardsChosen(player1, List.of(manifestedCard.getId()));

        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(permanent ->
                permanent.isManifested() && permanent.getCard().getId().equals(manifestedCard.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
    }

    @Test
    void tunnelOfHateGivesOneAttackingCreatureDoubleStrikeUntilEndOfTurn() {
        castRoom(1);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(gd.playerBattlefields.get(player1.getId()).indexOf(attacker)));
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).containsExactly(attacker.getId());

        harness.handlePermanentChosen(player1, attacker.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, nonAttacker, DOUBLE_STRIKE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, DOUBLE_STRIKE)).isFalse();
    }

    private void castRoom(int doorIndex) {
        harness.setHand(player1, List.of(new TicketBoothTunnelOfHate()));
        harness.addMana(player1, ManaColor.RED, doorIndex == 0 ? 3 : 6);
        harness.castModalSorcery(player1, 0, doorIndex, List.of());
        harness.passBothPriorities();
    }
}
