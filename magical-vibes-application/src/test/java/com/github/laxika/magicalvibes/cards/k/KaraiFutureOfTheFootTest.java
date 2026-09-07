package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({KaraiFutureOfTheFoot.class, GrizzlyBears.class})
class KaraiFutureOfTheFootTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage returns a creature card to hand when Karai was cast normally")
    void normalCastReturnsCreatureToHand() {
        Card target = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(target));
        Permanent karai = addCreatureReady(player1, new KaraiFutureOfTheFoot());
        karai.setAttacking(true);

        dealCombatDamage();
        chooseTarget(target);

        assertThat(gd.playerHands.get(player1.getId())).anyMatch(card -> card.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(permanent -> permanent.getCard().getId().equals(target.getId()));
    }

    @Test
    @DisplayName("Sneak combat damage returns a creature card to the battlefield")
    void sneakCastReturnsCreatureToBattlefield() {
        Card target = new GrizzlyBears();
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
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

    private void dealCombatDamage() {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
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
