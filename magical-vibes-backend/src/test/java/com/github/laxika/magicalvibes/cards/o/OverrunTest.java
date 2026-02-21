package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
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
import com.github.laxika.magicalvibes.model.effect.BoostAllOwnCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class OverrunTest {

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
    @DisplayName("Overrun has correct card properties")
    void hasCorrectProperties() {
        Overrun card = new Overrun();

        assertThat(card.getName()).isEqualTo("Overrun");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{2}{G}{G}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.isNeedsTarget()).isFalse();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(BoostAllOwnCreaturesEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(GrantKeywordEffect.class);
        GrantKeywordEffect grant = (GrantKeywordEffect) card.getEffects(EffectSlot.SPELL).get(1);
        assertThat(grant.keyword()).isEqualTo(Keyword.TRAMPLE);
        assertThat(grant.scope()).isEqualTo(GrantKeywordEffect.Scope.OWN_CREATURES);
        assertThat(card.getCardText()).contains("Creatures you control get +3/+3 and gain trample until end of turn.");
    }

    @Test
    @DisplayName("Resolving Overrun gives own creatures +3/+3 and trample")
    void resolvesAndBuffsOwnCreatures() {
        Permanent p1a = addReadyCreature(player1, new GrizzlyBears());
        Permanent p1b = addReadyCreature(player1, new GrizzlyBears());
        Permanent p2 = addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Overrun()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(p1a.getEffectivePower()).isEqualTo(5);
        assertThat(p1a.getEffectiveToughness()).isEqualTo(5);
        assertThat(p1b.getEffectivePower()).isEqualTo(5);
        assertThat(p1b.getEffectiveToughness()).isEqualTo(5);
        assertThat(p1a.hasKeyword(Keyword.TRAMPLE)).isTrue();
        assertThat(p1b.hasKeyword(Keyword.TRAMPLE)).isTrue();

        assertThat(p2.getEffectivePower()).isEqualTo(2);
        assertThat(p2.getEffectiveToughness()).isEqualTo(2);
        assertThat(p2.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Overrun trample assigns excess damage to defending player")
    void trampleAssignsExcessDamageToDefender() {
        harness.setLife(player2, 20);
        Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
        Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new Overrun()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        attacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // 5/5 trample blocked by 2/2 → assign lethal to blocker, excess to player
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 2,
                player2.getId(), 3
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId())).anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Overrun effects wear off at end of turn")
    void effectsWearOffAtEndOfTurn() {
        Permanent creature = addReadyCreature(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Overrun()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(5);
        assertThat(creature.getEffectiveToughness()).isEqualTo(5);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(creature.getEffectivePower()).isEqualTo(2);
        assertThat(creature.getEffectiveToughness()).isEqualTo(2);
        assertThat(creature.hasKeyword(Keyword.TRAMPLE)).isFalse();
    }

    @Test
    @DisplayName("Casting Overrun puts it on stack as sorcery spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new Overrun()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Overrun");
    }

    private Permanent addReadyCreature(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
