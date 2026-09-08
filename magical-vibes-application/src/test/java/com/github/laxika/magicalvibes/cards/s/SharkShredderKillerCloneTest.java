package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SharkShredderKillerClone.class, GrizzlyBears.class})
class SharkShredderKillerCloneTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage returns a creature from the damaged player's graveyard tapped and attacking")
    void combatDamageReturnsCreatureTappedAndAttacking() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        Permanent shark = addReadyShark();
        shark.setAttacking(true);
        shark.setAttackTarget(player2.getId());
        gd.playerAutoStopSteps.computeIfAbsent(player1.getId(), ignored -> new java.util.HashSet<>())
                .add(TurnStep.COMBAT_DAMAGE);
        gd.playerAutoStopSteps.computeIfAbsent(player2.getId(), ignored -> new java.util.HashSet<>())
                .add(TurnStep.COMBAT_DAMAGE);

        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
        resolveAllTriggers();

        Permanent returned = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().getId().equals(bears.getId()))
                .findFirst().orElseThrow();
        assertThat(returned.isTapped()).isTrue();
        assertThat(returned.isAttackedThisTurn()).isTrue();
        assertThat(returned.getAttackTarget()).isEqualTo(player2.getId());
        assertThat(gd.playerGraveyards.get(player2.getId())).noneMatch(card -> card.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Sneak puts Shark Shredder onto the battlefield tapped and attacking")
    void sneaksOntoTheBattlefield() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        harness.setHand(player1, List.of(new SharkShredderKillerClone()));
        harness.addMana(player1, ManaColor.BLACK, 5);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.castWithAlternateCost(player1, 0, List.of(attacker.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard().getClass() == SharkShredderKillerClone.class
                        && permanent.isTapped()
                        && permanent.isAttacking()
                        && permanent.getAttackTarget().equals(player2.getId()));
        assertThat(gd.playerHands.get(player1.getId()))
                .extracting(Card::getClass)
                .doesNotContain(SharkShredderKillerClone.class);
    }

    @Test
    @DisplayName("Only creatures in the damaged player's graveyard can be returned")
    void onlyDamagedPlayersCreaturesAreEligible() {
        Card ownBears = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(ownBears));
        Permanent shark = addReadyShark();
        shark.setAttacking(true);
        shark.setAttackTarget(player2.getId());

        resolveCombat();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(card -> card.getId().equals(ownBears.getId()));
    }

    private Permanent addReadyShark() {
        Permanent shark = addCreatureReady(player1, new SharkShredderKillerClone());
        shark.setSummoningSick(false);
        return shark;
    }
}
