package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MirriCatWarriorTest {

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
    @DisplayName("Mirri, Cat Warrior has correct card properties")
    void hasCorrectProperties() {
        MirriCatWarrior card = new MirriCatWarrior();

        assertThat(card.getName()).isEqualTo("Mirri, Cat Warrior");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{1}{G}{G}");
        assertThat(card.getColor()).isEqualTo(CardColor.GREEN);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(3);
        assertThat(card.getSupertypes()).containsExactly(CardSupertype.LEGENDARY);
        assertThat(card.getSubtypes()).containsExactlyInAnyOrder(CardSubtype.CAT, CardSubtype.WARRIOR);
        assertThat(card.getKeywords()).containsExactlyInAnyOrder(
                Keyword.FIRST_STRIKE,
                Keyword.FORESTWALK,
                Keyword.VIGILANCE
        );
    }

    @Test
    @DisplayName("Casting Mirri, Cat Warrior puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new MirriCatWarrior()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Mirri, Cat Warrior");
    }

    @Test
    @DisplayName("Forestwalk: Mirri cannot be blocked if defending player controls a Forest")
    void forestwalkCannotBeBlockedWhenDefenderHasForest() {
        harness.addToBattlefield(player2, new Forest());

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent mirri = new Permanent(new MirriCatWarrior());
        mirri.setSummoningSick(false);
        mirri.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(mirri);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(mirri);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("Forestwalk: Mirri can be blocked if defending player controls no Forest")
    void forestwalkAllowsBlockingWhenDefenderHasNoForest() {
        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        Permanent mirri = new Permanent(new MirriCatWarrior());
        mirri.setSummoningSick(false);
        mirri.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(mirri);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.BLOCKER_DECLARATION);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance: Mirri does not tap when declared as attacker")
    void vigilancePreventsTapWhenAttacking() {
        Permanent mirri = new Permanent(new MirriCatWarrior());
        mirri.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(mirri);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.setAwaitingInput(AwaitingInput.ATTACKER_DECLARATION);

        gs.declareAttackers(gd, player1, List.of(0));

        assertThat(mirri.isTapped()).isFalse();
    }

    @Test
    @DisplayName("First strike: Mirri kills 2/2 blocker before regular damage")
    void firstStrikeKillsBlockerBeforeRegularDamage() {
        Permanent mirri = new Permanent(new MirriCatWarrior());
        mirri.setSummoningSick(false);
        mirri.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(mirri);

        Permanent blocker = new Permanent(new GrizzlyBears());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Mirri, Cat Warrior"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }
}
