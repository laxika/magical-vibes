package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.a.ActOfTreason;
import com.github.laxika.magicalvibes.cards.d.DoomBlade;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PredatorySludge.class, GrizzlyBears.class, Swamp.class, DoomBlade.class,
        Unsummon.class, ActOfTreason.class})
class PredatorySludgeTest extends BaseCardTest {

    @Test
    void choosesAnOpponentPermanentAndConjuresWhenThatPermanentDies() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent unchosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent opponentLand = harness.addToBattlefieldAndReturn(player2, new Swamp());
        Permanent ownPermanent = harness.addToBattlefieldAndReturn(player1, new Swamp());
        Card sludge = new PredatorySludge();
        harness.castFromHand(player1, sludge, "{2}{B}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validPermanentIds())
                .containsExactlyInAnyOrder(chosen.getId(), unchosen.getId(), opponentLand.getId());
        assertThat(choice.validPermanentIds()).doesNotContain(ownPermanent.getId());
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownPermanent.getId()))
                .isInstanceOf(IllegalStateException.class);

        harness.handlePermanentChosen(player1, chosen.getId());
        resolveAllTriggers();

        destroy(unchosen);
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getName().equals("Predatory Sludge"));

        destroy(chosen);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getName().equals("Predatory Sludge")
                        && !card.getId().equals(sludge.getId()));
    }

    @Test
    void conjuresAfterSludgeHasLeftTheBattlefield() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent sludge = enterChoosing(chosen);

        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0, sludge.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(sludge);

        destroy(chosen);

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Predatory Sludge"))
                .hasSize(1)
                .allMatch(card -> !card.getId().equals(sludge.getCard().getId()));
    }

    @Test
    void conjuresWhenChosenPermanentDiesUnderYourControl() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        enterChoosing(chosen);

        harness.setHand(player1, List.of(new ActOfTreason()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, chosen.getId());
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(chosen);

        destroy(chosen);

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Predatory Sludge"))
                .hasSize(1);
    }

    @Test
    void delayedTriggerKeepsItsOriginalControllerAfterSludgeChangesControl() {
        Permanent chosen = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent sludge = enterChoosing(chosen);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new ActOfTreason()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player2, 0, sludge.getId());
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(sludge);

        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, chosen.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .filteredOn(card -> card.getName().equals("Predatory Sludge"))
                .hasSize(1);
        assertThat(gd.playerHands.get(player2.getId()))
                .noneMatch(card -> card.getName().equals("Predatory Sludge"));
    }

    @Test
    void entersWithoutAChoiceWhenOpponentControlsNoPermanents() {
        harness.castFromHand(player1, new PredatorySludge(), "{2}{B}");
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getName().equals("Predatory Sludge"));
    }

    private Permanent enterChoosing(Permanent chosen) {
        harness.castFromHand(player1, new PredatorySludge(), "{2}{B}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, chosen.getId());
        resolveAllTriggers();
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Predatory Sludge"))
                .findFirst().orElseThrow();
    }

    private void destroy(Permanent target) {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new DoomBlade()));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.castInstant(player1, 0, target.getId());
        resolveAllTriggers();
    }
}
