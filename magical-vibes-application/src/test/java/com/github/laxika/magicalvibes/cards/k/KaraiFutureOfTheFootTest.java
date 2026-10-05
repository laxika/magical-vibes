package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.p.PunkFrogs;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KaraiFutureOfTheFoot.class, PunkFrogs.class, Island.class})
class KaraiFutureOfTheFootTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage returns a creature card to hand when Karai was cast normally")
    void normalCastReturnsCreatureToHand() {
        Card target = new PunkFrogs();
        harness.setGraveyard(player1, List.of(target));
        Permanent karai = addCreatureReady(player1, new KaraiFutureOfTheFoot());
        karai.setAttacking(true);
        karai.setAttackTarget(player2.getId());

        resolveCombat();
        chooseTarget(target);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Sneak combat damage returns a creature card to the battlefield")
    void sneakCastReturnsCreatureToBattlefield() {
        Card target = new PunkFrogs();
        Permanent attacker = addCreatureReady(player1, new PunkFrogs());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setGraveyard(player1, List.of(target));
        harness.setHand(player1, List.of(new KaraiFutureOfTheFoot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();
        chooseTarget(target);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Sneak paid on an earlier turn returns the creature to hand")
    void sneakPaidOnEarlierTurnReturnsCreatureToHand() {
        Permanent attacker = addCreatureReady(player1, new PunkFrogs());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new KaraiFutureOfTheFoot()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();
        resolveCombat();
        resolveAllTriggers();
        harness.passUntilWithNoAttackers(player2, TurnStep.PRECOMBAT_MAIN);
        harness.passUntilWithNoAttackers(player1, TurnStep.PRECOMBAT_MAIN);

        Card target = new PunkFrogs();
        harness.setGraveyard(player1, List.of(target));
        Permanent karai = findPermanent(player1, "Karai, Future of the Foot");
        karai.setAttacking(true);
        karai.setAttackTarget(player2.getId());
        resolveCombat();
        chooseTarget(target);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Combat damage targets only creatures in Karai's controller's graveyard")
    void onlyOwnCreatureCardsAreEligible() {
        Card target = new PunkFrogs();
        Card land = new Island();
        Card opposingCreature = new PunkFrogs();
        harness.setGraveyard(player1, List.of(target, land));
        harness.setGraveyard(player2, List.of(opposingCreature));
        Permanent karai = addCreatureReady(player1, new KaraiFutureOfTheFoot());
        karai.setAttacking(true);
        karai.setAttackTarget(player2.getId());

        resolveCombat();
        chooseTarget(target);

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(land);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opposingCreature);
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(target.getId()));
    }

    @Test
    @DisplayName("A graveyard target removed before resolution is not returned")
    void removedTargetIsNotReturned() {
        Card target = new PunkFrogs();
        harness.setGraveyard(player1, List.of(target));
        Permanent karai = addCreatureReady(player1, new KaraiFutureOfTheFoot());
        karai.setAttacking(true);
        karai.setAttackTarget(player2.getId());
        resolveCombat();
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        assertThat(gd.stack).isNotEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(target);
        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(target));

        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId()))
                .noneMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    private void chooseTarget(Card target) {
        PendingInteraction.MultiGraveyardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(target.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        resolveAllTriggers();
    }
}
