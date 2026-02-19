package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SparkElementalTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameService gs;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gs = harness.getGameService();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    @Test
    @DisplayName("Spark Elemental has correct card properties and end-step trigger")
    void hasCorrectProperties() {
        SparkElemental card = new SparkElemental();

        assertThat(card.getName()).isEqualTo("Spark Elemental");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.getPower()).isEqualTo(3);
        assertThat(card.getToughness()).isEqualTo(1);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.ELEMENTAL);
        assertThat(card.getKeywords()).containsExactlyInAnyOrder(Keyword.TRAMPLE, Keyword.HASTE);
        assertThat(card.getEffects(EffectSlot.END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(SacrificeSelfEffect.class);
    }

    @Test
    @DisplayName("Casting puts it on the stack as CREATURE_SPELL")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new SparkElemental()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Spark Elemental");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new SparkElemental()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Can attack immediately due to haste")
    void canAttackImmediatelyDueToHaste() {
        harness.setLife(player2, 20);

        Permanent spark = new Permanent(new SparkElemental());
        spark.setSummoningSick(true);
        gd.playerBattlefields.get(player1.getId()).add(spark);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Trample assigns excess combat damage to defending player")
    void trampleAssignsExcessDamageToDefendingPlayer() {
        harness.setLife(player2, 20);

        Permanent spark = new Permanent(new SparkElemental());
        spark.setSummoningSick(false);
        spark.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(spark);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerGraveyards.get(player1.getId())).anyMatch(c -> c.getName().equals("Spark Elemental"));
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Triggers at end step and sacrifices itself on resolution")
    void triggersAtEndStepAndSacrificesItself() {
        Permanent spark = new Permanent(new SparkElemental());
        gd.playerBattlefields.get(player1.getId()).add(spark);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard().getName()).isEqualTo("Spark Elemental");
        assertThat(trigger.getSourcePermanentId()).isEqualTo(spark.getId());

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Spark Elemental"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Spark Elemental"));
    }
}
