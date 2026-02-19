package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.AdditionalCombatMainPhaseEffect;
import com.github.laxika.magicalvibes.model.effect.UntapAttackedCreaturesEffect;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RelentlessAssaultTest {

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

    @Test
    @DisplayName("Relentless Assault has correct card properties")
    void hasCorrectProperties() {
        RelentlessAssault card = new RelentlessAssault();

        assertThat(card.getName()).isEqualTo("Relentless Assault");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{2}{R}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(UntapAttackedCreaturesEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(AdditionalCombatMainPhaseEffect.class);
        AdditionalCombatMainPhaseEffect extra = (AdditionalCombatMainPhaseEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(extra.count()).isEqualTo(1);
    }

    @Test
    @DisplayName("Casting puts Relentless Assault on the stack as a sorcery with no target")
    void castingPutsItOnStack() {
        harness.setHand(player1, List.of(new RelentlessAssault()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Relentless Assault");
        assertThat(entry.getTargetPermanentId()).isNull();
    }

    @Test
    @DisplayName("Resolving untaps only creatures that attacked this turn")
    void resolvingUntapsOnlyAttackedCreatures() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new GrizzlyBears());
        List<Permanent> battlefield = harness.getGameData().playerBattlefields.get(player1.getId());
        battlefield.forEach(p -> p.setSummoningSick(false));

        declareAttackers(player1, List.of(0));
        Permanent attackedBear = battlefield.get(0);
        Permanent nonAttackedBear = battlefield.get(1);
        nonAttackedBear.tap();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.setHand(player1, List.of(new RelentlessAssault()));
        harness.addMana(player1, ManaColor.RED, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(attackedBear.isTapped()).isFalse();
        assertThat(nonAttackedBear.isTapped()).isTrue();
        assertThat(harness.getGameData().additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    @DisplayName("Additional combat begins after postcombat main when Relentless Assault resolves")
    void additionalCombatBeginsAfterPostcombatMain() {
        harness.setHand(player1, List.of(new RelentlessAssault()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.BEGINNING_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_OF_COMBAT);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.POSTCOMBAT_MAIN);

        harness.getGameService().advanceStep(gd);
        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
    }

    @Test
    @DisplayName("Attacked-this-turn status resets on turn change")
    void attackedThisTurnResetsOnTurnChange() {
        harness.addToBattlefield(player1, new GrizzlyBears());
        Permanent bear = harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
        bear.setSummoningSick(false);

        declareAttackers(player1, List.of(0));
        assertThat(bear.isAttackedThisTurn()).isTrue();

        harness.forceStep(TurnStep.CLEANUP);
        harness.getGameService().advanceStep(harness.getGameData());
        assertThat(bear.isAttackedThisTurn()).isFalse();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        bear.tap();

        harness.setHand(player1, List.of(new RelentlessAssault()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(bear.isTapped()).isTrue();
    }

    private void declareAttackers(Player attacker, List<Integer> attackers) {
        GameData gd = harness.getGameData();
        harness.forceActivePlayer(attacker);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;
        harness.getGameService().declareAttackers(gd, attacker, attackers);
    }
}
