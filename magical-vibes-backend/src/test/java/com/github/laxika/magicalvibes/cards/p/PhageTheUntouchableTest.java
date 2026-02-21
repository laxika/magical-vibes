package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BeaconOfUnrest;
import com.github.laxika.magicalvibes.cards.m.MahamotiDjinn;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.LoseGameIfNotCastFromHandEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerLosesGameEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PhageTheUntouchableTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    @Test
    @DisplayName("Phage the Untouchable has correct card properties")
    void hasCorrectProperties() {
        PhageTheUntouchable card = new PhageTheUntouchable();

        assertThat(card.getName()).isEqualTo("Phage the Untouchable");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}{B}{B}{B}{B}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLACK);
        assertThat(card.getPower()).isEqualTo(4);
        assertThat(card.getToughness()).isEqualTo(4);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.AVATAR, CardSubtype.MINION);
        assertThat(card.getEffects(EffectSlot.ON_ENTER_BATTLEFIELD).getFirst()).isInstanceOf(LoseGameIfNotCastFromHandEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_CREATURE).getFirst()).isInstanceOf(DestroyTargetPermanentEffect.class);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst()).isInstanceOf(TargetPlayerLosesGameEffect.class);
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

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
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
