package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.AvenFisher;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfFire;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedOnlyByFilterEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ElvenRidersTest {

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
    @DisplayName("Elven Riders has correct card properties")
    void hasCorrectProperties() {
        ElvenRiders card = new ElvenRiders();

        assertThat(card.getName()).isEqualTo("Elven Riders");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}{G}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getPower()).isEqualTo(3);
        assertThat(card.getToughness()).isEqualTo(3);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.ELF);
        assertThat(card.getEffects(EffectSlot.STATIC)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.STATIC).getFirst()).isInstanceOf(CanBeBlockedOnlyByFilterEffect.class);
        CanBeBlockedOnlyByFilterEffect effect = (CanBeBlockedOnlyByFilterEffect) card.getEffects(EffectSlot.STATIC).getFirst();
        assertThat(effect.blockerPredicate()).isInstanceOf(PermanentAnyOfPredicate.class);
        assertThat(effect.allowedBlockersDescription()).isEqualTo("creatures with flying or Walls");
    }

    @Test
    @DisplayName("Elven Riders cannot be blocked by non-Wall non-flying creature")
    void cannotBeBlockedByNormalCreature() {
        Permanent riders = attackingRiders();
        gd.playerBattlefields.get(player1.getId()).add(riders);

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying or Walls");
    }

    @Test
    @DisplayName("Elven Riders can be blocked by a Wall")
    void canBeBlockedByWall() {
        Permanent riders = attackingRiders();
        gd.playerBattlefields.get(player1.getId()).add(riders);

        Permanent wall = new Permanent(new WallOfFire());
        wall.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wall);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    @Test
    @DisplayName("Elven Riders can be blocked by a creature with flying")
    void canBeBlockedByFlyingCreature() {
        Permanent riders = attackingRiders();
        gd.playerBattlefields.get(player1.getId()).add(riders);

        Permanent flyer = new Permanent(new AvenFisher());
        flyer.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(flyer);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(flyer.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Reach is not enough to block Elven Riders")
    void reachIsNotEnoughToBlock() {
        Permanent riders = attackingRiders();
        gd.playerBattlefields.get(player1.getId()).add(riders);

        Permanent spider = new Permanent(new GiantSpider());
        spider.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(spider);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by creatures with flying or Walls");
    }

    private Permanent attackingRiders() {
        Permanent riders = new Permanent(new ElvenRiders());
        riders.setSummoningSick(false);
        riders.setAttacking(true);
        return riders;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
