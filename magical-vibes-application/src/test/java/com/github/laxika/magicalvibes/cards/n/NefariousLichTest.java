package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.d.DuskImp;
import com.github.laxika.magicalvibes.cards.f.FlameBurst;
import com.github.laxika.magicalvibes.cards.w.WurmcoilEngine;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NefariousLich.class, DuskImp.class, FlameBurst.class, WurmcoilEngine.class})
class NefariousLichTest extends BaseCardTest {

    @Test
    @DisplayName("Damage is replaced by exiling exactly that many graveyard cards")
    void damageExilesExactlyThatManyCards() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new NefariousLich());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameBurst()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Insufficient graveyard cards cause the controller to lose without partial exiling")
    void insufficientGraveyardCardsCauseLossWithoutPartialExiling() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new NefariousLich());
        harness.setGraveyard(player1, List.of(new DuskImp()));

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameBurst()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Combat damage is replaced by exiling graveyard cards")
    void combatDamageExilesGraveyardCards() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new NefariousLich());
        harness.setGraveyard(player1, List.of(new DuskImp(), new DuskImp(), new DuskImp()));

        Permanent attacker = addCreatureReady(player2, new DuskImp());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertLife(player1, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(2);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Replaced combat damage does not cause lifelink life gain")
    void replacedCombatDamageDoesNotCauseLifelink() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new NefariousLich());
        harness.setGraveyard(player1, List.of(
                new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp(), new DuskImp()));

        Permanent attacker = addCreatureReady(player2, new WurmcoilEngine());
        attacker.setAttacking(true);

        resolveCombat(player2);

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(6);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("Damage to a creature is not replaced")
    void damageToCreatureIsNotReplaced() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new NefariousLich());
        harness.setGraveyard(player1, List.of(new DuskImp()));
        Permanent target = addCreatureReady(player1, new DuskImp());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new FlameBurst()));
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, target.getId());

        harness.assertLife(player1, 20);
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(target);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Life gain is replaced by drawing that many cards")
    void lifeGainDrawsCards() {
        harness.addToBattlefield(player1, new NefariousLich());
        harness.setLife(player1, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player1.getId(), 3));

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Increasing the life total is replaced by drawing that many cards")
    void increasingLifeTotalDrawsCards() {
        harness.addToBattlefield(player1, new NefariousLich());
        harness.setLife(player1, 20);

        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        harness.inMutationScope(() -> harness.getLifeSupport().applySetLifeTotal(gd, player1.getId(), 23));

        harness.assertLife(player1, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore + 3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 3);
    }

    @Test
    @DisplayName("Life loss is not replaced by exiling graveyard cards")
    void lifeLossIsNotReplaced() {
        harness.addToBattlefield(player1, new NefariousLich());
        harness.setLife(player1, 20);
        harness.setGraveyard(player1, List.of(new DuskImp()));

        harness.inMutationScope(() -> harness.getLifeSupport().applyLifeLoss(gd, player1.getId(), 3, "test"));

        harness.assertLife(player1, 17);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Life gain by another player is not replaced")
    void opponentsLifeGainIsNotReplaced() {
        harness.addToBattlefield(player1, new NefariousLich());
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        int opponentHandSizeBefore = gd.playerHands.get(player2.getId()).size();
        int opponentDeckSizeBefore = gd.playerDecks.get(player2.getId()).size();

        harness.inMutationScope(() -> harness.getLifeSupport().applyGainLife(gd, player2.getId(), 3));

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 23);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(opponentHandSizeBefore);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckSizeBefore);
    }

    @Test
    @DisplayName("Nefarious Lich does not prevent losing for having zero life")
    void doesNotPreventZeroLifeLoss() {
        harness.addToBattlefield(player1, new NefariousLich());
        harness.setLife(player1, 0);

        harness.runStateBasedActions();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Controller loses the game when Nefarious Lich leaves the battlefield")
    void controllerLosesWhenLichLeavesBattlefield() {
        Permanent lich = harness.addToBattlefieldAndReturn(player1, new NefariousLich());
        harness.setLife(player1, 20);

        harness.inMutationScope(
                () -> harness.getPermanentRemovalService().removePermanentToGraveyard(gd, lich));

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
