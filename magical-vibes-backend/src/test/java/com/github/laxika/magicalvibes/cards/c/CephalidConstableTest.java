package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentsOnCombatDamageToPlayerEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CephalidConstableTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        harness.skipMulligan();
        harness.clearMessages();
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        GameData gd = harness.getGameData();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void resolveCombat() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Cephalid Constable has correct card properties")
    void hasCorrectProperties() {
        CephalidConstable card = new CephalidConstable();

        assertThat(card.getName()).isEqualTo("Cephalid Constable");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{U}{U}");
        assertThat(card.getColor()).isEqualTo(CardColor.BLUE);
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.OCTOPUS, CardSubtype.WIZARD);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_COMBAT_DAMAGE_TO_PLAYER).getFirst()).isInstanceOf(ReturnPermanentsOnCombatDamageToPlayerEffect.class);
    }

    // ===== Combat damage trigger =====

    @Test
    @DisplayName("Dealing combat damage to player triggers multi-permanent choice")
    void combatDamageTriggersBounce() {
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
        assertThat(gd.awaitingMultiPermanentChoicePlayerId).isEqualTo(player1.getId());
        assertThat(gd.awaitingMultiPermanentChoiceMaxCount).isEqualTo(1);
    }

    @Test
    @DisplayName("Choosing a permanent to bounce returns it to owner's hand")
    void bouncePermanent() {
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        UUID bearsId = bears.getId();

        harness.handleMultiplePermanentsChosen(player1, List.of(bearsId));

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("Grizzly Bears") && log.contains("returned"));
    }

    @Test
    @DisplayName("Choosing zero permanents is allowed (up to)")
    void chooseZeroPermanents() {
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();

        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.gameLog).anyMatch(log -> log.contains("chooses not to return"));
    }

    @Test
    @DisplayName("No trigger when defender has no permanents")
    void noTriggerWhenNoPermanents() {
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        // player2 has no permanents

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isNull();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("has no permanents"));
    }

    @Test
    @DisplayName("No trigger when Constable is blocked and deals no damage to player")
    void noTriggerWhenBlocked() {
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isNotEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);
    }

    @Test
    @DisplayName("Cannot select more permanents than damage dealt")
    void cannotSelectMoreThanDamage() {
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent bears1 = addReadyCreature(player2, new GrizzlyBears());
        Permanent bears2 = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingMultiPermanentChoiceMaxCount).isEqualTo(1);

        List<UUID> allIds = List.of(bears1.getId(), bears2.getId());
        assertThatThrownBy(() -> harness.handleMultiplePermanentsChosen(player1, allIds))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Too many");
    }

    @Test
    @DisplayName("Game advances after bounce choice is made")
    void gameAdvancesAfterChoice() {
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        Permanent bears = addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        assertThat(gd.awaitingInput).isEqualTo(AwaitingInput.MULTI_PERMANENT_CHOICE);

        harness.handleMultiplePermanentsChosen(player1, List.of(bears.getId()));

        // Game should have advanced past combat damage (auto-passes through END_OF_COMBAT)
        assertThat(gd.awaitingInput).isNull();
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);
    }

    @Test
    @DisplayName("Defender takes 1 combat damage from unblocked Constable")
    void defenderTakesCombatDamage() {
        harness.setLife(player2, 20);
        Permanent constable = addReadyCreature(player1, new CephalidConstable());
        constable.setAttacking(true);
        addReadyCreature(player2, new GrizzlyBears());

        resolveCombat();

        GameData gd = harness.getGameData();
        harness.handleMultiplePermanentsChosen(player1, List.of());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }
}
