package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LureTest {

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
    @DisplayName("Lure has correct card properties")
    void hasCorrectProperties() {
        Lure card = new Lure();

        assertThat(card.getName()).isEqualTo("Lure");
        assertThat(card.getType()).isEqualTo(CardType.ENCHANTMENT);
        assertThat(card.getManaCost()).isEqualTo("{1}{G}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.AURA);
        assertThat(card.getCardText()).contains("All creatures able to block enchanted creature do so.");
        assertThat(card.isNeedsTarget()).isTrue();
        assertThat(card.isAura()).isTrue();
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(MustBeBlockedByAllCreaturesEffect.class);
    }

    @Test
    @DisplayName("All able creatures must block enchanted attacker")
    void allAbleCreaturesMustBlock() {
        Permanent enchantedAttacker = attackingCreature(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(enchantedAttacker);
        Permanent lure = new Permanent(new Lure());
        lure.setAttachedTo(enchantedAttacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(lure);

        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));
        gd.playerBattlefields.get(player2.getId()).add(readyCreature(new GrizzlyBears()));

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declares 2 blockers"));
    }

    @Test
    @DisplayName("Tapped creatures are not forced to block by Lure")
    void tappedCreaturesNotForcedToBlock() {
        Permanent enchantedAttacker = attackingCreature(new GrizzlyBears());
        gd.playerBattlefields.get(player1.getId()).add(enchantedAttacker);
        Permanent lure = new Permanent(new Lure());
        lure.setAttachedTo(enchantedAttacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(lure);

        Permanent untapped = readyCreature(new GrizzlyBears());
        Permanent tapped = readyCreature(new GrizzlyBears());
        tapped.tap();
        gd.playerBattlefields.get(player2.getId()).add(untapped);
        gd.playerBattlefields.get(player2.getId()).add(tapped);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(untapped.isBlocking()).isTrue();
        assertThat(tapped.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("Creatures unable to block enchanted attacker are not forced by Lure")
    void unableBlockersNotForced() {
        Permanent enchantedAttacker = attackingCreature(new AvenFisher());
        gd.playerBattlefields.get(player1.getId()).add(enchantedAttacker);
        Permanent lure = new Permanent(new Lure());
        lure.setAttachedTo(enchantedAttacker.getId());
        gd.playerBattlefields.get(player1.getId()).add(lure);

        Permanent nonFlying = readyCreature(new GrizzlyBears());
        Permanent flying = readyCreature(new AvenFisher());
        gd.playerBattlefields.get(player2.getId()).add(nonFlying);
        gd.playerBattlefields.get(player2.getId()).add(flying);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("must block enchanted creature if able");

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));

        assertThat(nonFlying.isBlocking()).isFalse();
        assertThat(flying.isBlocking()).isTrue();
    }

    private Permanent attackingCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        return permanent;
    }

    private Permanent readyCreature(com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        return permanent;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
