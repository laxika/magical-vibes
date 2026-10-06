package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.j.JaceBeleren;
import com.github.laxika.magicalvibes.cards.m.MouserMarkIII;
import com.github.laxika.magicalvibes.cards.w.WhiteKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SharkShredderKillerClone.class, GrizzlyBears.class, JaceBeleren.class,
        MouserMarkIII.class, WhiteKnight.class})
class SharkShredderKillerCloneTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage returns a creature from the damaged player's graveyard tapped and attacking")
    void combatDamageReturnsCreatureTappedAndAttacking() {
        Card bears = new GrizzlyBears();
        harness.setGraveyard(player2, List.of(bears));
        Permanent shark = addReadyShark();
        shark.setAttacking(true);
        shark.setAttackTarget(player2.getId());
        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            resolveCombat();
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));
            resolveAllTriggers();
        });

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

    @Test
    @DisplayName("Returning a creature is optional even with an eligible card")
    void mayChooseNoCreature() {
        Card creature = new MouserMarkIII();
        harness.setGraveyard(player2, List.of(creature));
        Permanent shark = addReadyShark();
        shark.setAttacking(true);
        shark.setAttackTarget(player2.getId());

        resolveCombat();
        assertThat(gd.interaction.isAwaitingInput()).isTrue();
        harness.handleMultipleCardsChosen(player1, List.of());
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player2.getId())).contains(creature);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(shark);
    }

    @Test
    @DisplayName("A noncreature card in the damaged player's graveyard is not eligible")
    void cannotReturnNoncreature() {
        Card planeswalker = new JaceBeleren();
        harness.setGraveyard(player2, List.of(planeswalker));
        Permanent shark = addReadyShark();
        shark.setAttacking(true);
        shark.setAttackTarget(player2.getId());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(planeswalker);
        assertThat(gd.playerBattlefields.get(player1.getId())).containsExactly(shark);
    }

    @Test
    @DisplayName("A returned creature deals damage in the regular combat damage step")
    void returnedCreatureDealsRegularDamage() {
        Card creature = new MouserMarkIII();
        harness.setLife(player2, 20);
        harness.setGraveyard(player2, List.of(creature));
        Permanent shark = addReadyShark();
        shark.setAttacking(true);
        shark.setAttackTarget(player2.getId());

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            resolveCombat();
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
            resolveAllTriggers();
            harness.passUntil(TurnStep.END_OF_COMBAT);
        });

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("A first-strike creature returned after first-strike damage still deals regular damage")
    void returnedFirstStrikerDealsRegularDamage() {
        Card creature = new WhiteKnight();
        harness.setLife(player2, 20);
        harness.setGraveyard(player2, List.of(creature));
        Permanent shark = addReadyShark();
        shark.setAttacking(true);
        shark.setAttackTarget(player2.getId());

        harness.withAutoStop(TurnStep.END_OF_COMBAT, () -> {
            resolveCombat();
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
            resolveAllTriggers();
            harness.passUntil(TurnStep.END_OF_COMBAT);
        });

        harness.assertLife(player2, 14);
    }

    @Test
    @DisplayName("The returned creature must attack the damaged player even if a planeswalker is present")
    void cannotChooseDifferentAttackTarget() {
        Card creature = new MouserMarkIII();
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new JaceBeleren());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.setGraveyard(player2, List.of(creature));
        Permanent shark = addReadyShark();
        shark.setAttacking(true);
        shark.setAttackTarget(player2.getId());

        harness.withAutoStop(TurnStep.COMBAT_DAMAGE, () -> {
            resolveCombat();
            assertThat(gd.interaction.isAwaitingInput()).isTrue();
            harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
            resolveAllTriggers();

            assertThat(gd.interaction.isAwaitingInput()).isFalse();
            Permanent returned = findPermanent(player1, "Mouser Mark III");
            assertThat(returned.isAttacking()).isTrue();
            assertThat(returned.getAttackTarget()).isEqualTo(player2.getId());
        });
    }

    private Permanent addReadyShark() {
        return addCreatureReady(player1, new SharkShredderKillerClone());
    }
}
