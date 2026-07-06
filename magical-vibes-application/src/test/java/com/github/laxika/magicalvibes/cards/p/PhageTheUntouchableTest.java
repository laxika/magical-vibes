package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfNotCastFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhageTheUntouchableTest extends BaseCardTest {

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    

    @Test
    @DisplayName("Casting Phage from hand does not make its controller lose the game")
    void castFromHandDoesNotLoseGame() {
        harness.setHand(player1, List.of(new PhageTheUntouchable()));
        harness.addMana(player1, ManaColor.BLACK, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Phage the Untouchable"));
    }

    @Test
    @DisplayName("Entering from graveyard causes controller to lose the game")
    void enteringWithoutCastingFromHandLosesGame() {
        harness.setGraveyard(player1, List.of(new PhageTheUntouchable()));
        harness.setHand(player1, List.of(new BeaconOfUnrest()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.GraveyardChoice.class);
        harness.handleGraveyardCardChosen(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("loses the game from Phage the Untouchable"));
    }

    @Test
    @DisplayName("Combat damage to player makes that player lose the game")
    void combatDamageToPlayerLosesGame() {
        Permanent phage = addReadyCreature(player1, new PhageTheUntouchable());
        phage.setAttacking(true);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("Bob loses the game from Phage the Untouchable"));
    }

    @Test
    @DisplayName("Combat damage to a creature destroys that creature")
    void combatDamageToCreatureDestroysCreature() {
        Permanent phage = addReadyCreature(player1, new PhageTheUntouchable());
        phage.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new MahamotiDjinn());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(card -> card.getName().equals("Mahamoti Djinn"));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Mahamoti Djinn"));
    }
}
