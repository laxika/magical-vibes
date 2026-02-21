package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.DestroyCreatureBlockingThisEffect;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SylvanBasiliskTest {

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
    @DisplayName("Sylvan Basilisk has correct card properties")
    void hasCorrectProperties() {
        SylvanBasilisk card = new SylvanBasilisk();

        assertThat(card.getName()).isEqualTo("Sylvan Basilisk");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}{G}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(4);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.BASILISK);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_BLOCKED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_BECOMES_BLOCKED).getFirst())
                .isInstanceOf(DestroyCreatureBlockingThisEffect.class);
    }

    @Test
    @DisplayName("When Sylvan Basilisk becomes blocked, it creates a non-targeting trigger for that blocker")
    void becomesBlockedCreatesNonTargetingTrigger() {
        Permanent basilisk = addReadyBasilisk(player1);
        basilisk.setAttacking(true);

        Permanent blocker = addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Sylvan Basilisk");
        assertThat(entry.getTargetPermanentId()).isEqualTo(blocker.getId());
        assertThat(entry.getSourcePermanentId()).isEqualTo(basilisk.getId());
        assertThat(entry.isNonTargeting()).isTrue();
    }

    @Test
    @DisplayName("Resolving trigger destroys the creature that blocked Sylvan Basilisk")
    void resolvingTriggerDestroysBlocker() {
        Permanent basilisk = addReadyBasilisk(player1);
        basilisk.setAttacking(true);

        Permanent blocker = addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getId().equals(blocker.getId()));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getId().equals(basilisk.getId()));
    }

    @Test
    @DisplayName("Sylvan Basilisk creates one trigger per blocking creature")
    void createsOneTriggerPerBlockingCreature() {
        addReadyBasilisk(player1).setAttacking(true);

        addReadyBears(player2);
        addReadyBears(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)
        ));

        long basiliskTriggerCount = gd.stack.stream()
                .filter(entry -> entry.getCard().getName().equals("Sylvan Basilisk"))
                .count();
        assertThat(basiliskTriggerCount).isEqualTo(2);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(c -> c.getName().equals("Grizzly Bears"))
                .hasSize(2);
    }

    private Permanent addReadyBasilisk(Player player) {
        Permanent perm = new Permanent(new SylvanBasilisk());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyBears(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);
    }
}
