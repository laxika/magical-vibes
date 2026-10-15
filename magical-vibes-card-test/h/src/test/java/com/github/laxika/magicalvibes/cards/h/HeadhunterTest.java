package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.b.BarkhideMauler;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Headhunter.class, BarkhideMauler.class})
class HeadhunterTest extends BaseCardTest {

    @Test
    void combatDamageMakesDamagedPlayerDiscard() {
        Permanent headhunter = addAttackingHeadhunter();
        harness.setHand(player2, new ArrayList<>(List.of(new BarkhideMauler(), new BarkhideMauler())));

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(headhunter.isFaceDown()).isFalse();
    }

    @Test
    void blockedHeadhunterDoesNotTrigger() {
        addAttackingHeadhunter();
        Permanent blocker = addCreatureReady(player2, new BarkhideMauler());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setHand(player2, new ArrayList<>(List.of(new BarkhideMauler())));

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void morphsFaceDownAndCanBeTurnedFaceUp() {
        harness.setHand(player1, List.of(new Headhunter()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent headhunter = findPermanent(player1, "Headhunter");
        assertThat(headhunter.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(headhunter));
        harness.passBothPriorities();

        assertThat(headhunter.isFaceDown()).isFalse();
    }

    @Test
    void faceDownHeadhunterDoesNotTrigger() {
        harness.setHand(player1, List.of(new Headhunter()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent headhunter = findPermanent(player1, "Headhunter");
        assertThat(headhunter.isFaceDown()).isTrue();
        headhunter.setSummoningSick(false);
        headhunter.setAttacking(true);
        harness.setHand(player2, List.of(new BarkhideMauler()));
        harness.setLibrary(player2, List.of());

        resolveCombat();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
    }

    @Test
    void damagedPlayerWithEmptyHandDiscardsNothing() {
        addAttackingHeadhunter();
        harness.setHand(player2, List.of());

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void discardTriggerStillResolvesAfterHeadhunterLeavesBattlefield() {
        Permanent headhunter = addAttackingHeadhunter();
        harness.setHand(player2, List.of(new BarkhideMauler(), new BarkhideMauler()));

        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();
        gd.playerBattlefields.get(player1.getId()).remove(headhunter);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 1);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void turningFaceUpBeforeCombatDamageRestoresDiscardAbility() {
        Permanent headhunter = addAttackingHeadhunter();
        headhunter.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.setHand(player2, List.of(new BarkhideMauler(), new BarkhideMauler()));
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.turnFaceUp(player1, 0);
        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void opponentControlledHeadhunterMakesPlayerOneDiscard() {
        Permanent headhunter = addCreatureReady(player2, new Headhunter());
        headhunter.setAttacking(true);
        harness.setHand(player1, List.of(new BarkhideMauler(), new BarkhideMauler()));

        resolveCombat(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
    }

    private Permanent addAttackingHeadhunter() {
        Permanent headhunter = addCreatureReady(player1, new Headhunter());
        headhunter.setAttacking(true);
        return headhunter;
    }
}
