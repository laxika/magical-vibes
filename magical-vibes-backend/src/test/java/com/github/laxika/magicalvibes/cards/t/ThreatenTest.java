package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.Pacifism;
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
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetCreatureUntilEndOfTurnEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordToTargetEffect;
import com.github.laxika.magicalvibes.model.effect.UntapTargetPermanentEffect;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ThreatenTest {

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

    @Test
    @DisplayName("Threaten has correct card properties")
    void hasCorrectProperties() {
        Threaten card = new Threaten();

        assertThat(card.getName()).isEqualTo("Threaten");
        assertThat(card.getType()).isEqualTo(CardType.SORCERY);
        assertThat(card.getManaCost()).isEqualTo("{2}{R}");
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(3);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0)).isInstanceOf(UntapTargetPermanentEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(GainControlOfTargetCreatureUntilEndOfTurnEffect.class);
        assertThat(card.getEffects(EffectSlot.SPELL).get(2)).isInstanceOf(GrantKeywordToTargetEffect.class);
    }

    @Test
    @DisplayName("Casting Threaten puts it on the stack with the target creature")
    void castingPutsOnStack() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new Threaten()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.SORCERY_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Threaten");
        assertThat(entry.getTargetPermanentId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Resolving Threaten untaps target, gains control, and grants haste")
    void resolvesUntapGainControlAndHaste() {
        Permanent target = addReadyCreature(player2);
        target.tap();
        harness.setHand(player1, List.of(new Threaten()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        assertThat(target.isTapped()).isFalse();
        assertThat(gd.playerBattlefields.get(player1.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.untilEndOfTurnStolenCreatures).contains(target.getId());
    }

    @Test
    @DisplayName("Stolen creature can attack this turn because Threaten grants haste")
    void stolenCreatureCanAttackDueToHaste() {
        Permanent target = addReadyCreature(player2);
        target.setSummoningSick(false);
        harness.setHand(player1, List.of(new Threaten()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        GameService gs = harness.getGameService();
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(target);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.awaitingInput = AwaitingInput.ATTACKER_DECLARATION;

        gs.declareAttackers(gd, player1, List.of(attackerIndex));

        assertThat(target.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Threaten control and haste expire at cleanup")
    void controlAndHasteExpireAtCleanup() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new Threaten()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).anyMatch(p -> p.getId().equals(target.getId()));
        assertThat(gd.playerBattlefields.get(player1.getId())).noneMatch(p -> p.getId().equals(target.getId()));
        assertThat(target.hasKeyword(Keyword.HASTE)).isFalse();
        assertThat(gd.untilEndOfTurnStolenCreatures).doesNotContain(target.getId());
    }

    @Test
    @DisplayName("Can target own creature (control change is a no-op)")
    void canTargetOwnCreature() {
        Permanent ownCreature = addReadyCreature(player1);
        ownCreature.tap();
        harness.setHand(player1, List.of(new Threaten()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.isTapped()).isFalse();
        assertThat(ownCreature.hasKeyword(Keyword.HASTE)).isTrue();
        assertThat(gd.untilEndOfTurnStolenCreatures).doesNotContain(ownCreature.getId());
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent enchantment = new Permanent(new Pacifism());
        gd.playerBattlefields.get(player2.getId()).add(enchantment);
        harness.setHand(player1, List.of(new Threaten()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, enchantment.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Threaten fizzles if target is removed before resolution")
    void fizzlesIfTargetRemoved() {
        Permanent target = addReadyCreature(player2);
        harness.setHand(player1, List.of(new Threaten()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castSorcery(player1, 0, target.getId());
        gd.playerBattlefields.get(player2.getId()).clear();

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.gameLog).anyMatch(log -> log.contains("fizzles"));
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}

