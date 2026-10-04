package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TrollAscetic;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FaebloomTrick.class, GrizzlyBears.class, TrollAscetic.class, Unsummon.class})
class FaebloomTrickTest extends BaseCardTest {

    @Test
    @DisplayName("Creates two flying Faeries and then taps a target opponent creature")
    void createsFaeriesThenTapsOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaebloomTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2)
                .allSatisfy(faerie -> {
                    assertThat(faerie.getCard().getColor()).isEqualTo(CardColor.BLUE);
                    assertThat(faerie.getCard().getSubtypes()).contains(CardSubtype.FAERIE);
                    assertThat(faerie.getCard().getKeywords()).contains(Keyword.FLYING);
                });
        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(opponentCreature.getId());
        assertThat(opponentCreature.isTapped()).isFalse();

        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Still creates Faeries when no opponent creature can be tapped")
    void createsFaeriesWithoutValidTapTarget() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaebloomTrick()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
    }

    @Test
    void canTargetAnAlreadyTappedOpponentCreature() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        opponentCreature.setTapped(true);
        harness.setHand(player1, List.of(new FaebloomTrick()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(opponentCreature.getId());
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        harness.passBothPriorities();

        assertThat(opponentCreature.isTapped()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tokensRemainWhenTapTargetLeavesInResponse() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new FaebloomTrick()));
        harness.setHand(player2, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0);
        harness.handlePermanentChosen(player1, opponentCreature.getId());
        assertThat(gd.stack).hasSize(1);
        assertThat(opponentCreature.isTapped()).isFalse();

        harness.castAndResolveInstant(player2, 0, opponentCreature.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotOfferHexproofOpponentCreatureAsTarget() {
        Permanent legalTarget = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.addToBattlefield(player2, new TrollAscetic());
        harness.setHand(player1, List.of(new FaebloomTrick()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(legalTarget.getId());
        harness.handlePermanentChosen(player1, legalTarget.getId());
        harness.passBothPriorities();
        assertThat(legalTarget.isTapped()).isTrue();
    }

    @Test
    void createsTokensWithoutTargetChoiceWhenOnlyOpponentCreatureHasHexproof() {
        harness.addToBattlefield(player2, new TrollAscetic());
        harness.setHand(player1, List.of(new FaebloomTrick()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).filteredOn(permanent -> permanent.getCard().isToken())
                .hasSize(2);
        assertThat(gd.stack).isEmpty();
    }
}
