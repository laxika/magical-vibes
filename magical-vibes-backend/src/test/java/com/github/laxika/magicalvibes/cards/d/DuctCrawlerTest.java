package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameService;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class DuctCrawlerTest {

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

    // ===== Card properties =====

    @Test
    @DisplayName("Duct Crawler has correct name")
    void hasCorrectName() {
        DuctCrawler card = new DuctCrawler();
        assertThat(card.getName()).isEqualTo("Duct Crawler");
    }

    @Test
    @DisplayName("Duct Crawler is a red creature")
    void isRedCreature() {
        DuctCrawler card = new DuctCrawler();
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getColor()).isEqualTo(CardColor.RED);
    }

    @Test
    @DisplayName("Duct Crawler is an Insect")
    void isInsect() {
        DuctCrawler card = new DuctCrawler();
        assertThat(card.getSubtypes()).contains(CardSubtype.INSECT);
    }

    @Test
    @DisplayName("Duct Crawler is 1/1")
    void isPowerToughnessOneOne() {
        DuctCrawler card = new DuctCrawler();
        assertThat(card.getPower()).isEqualTo(1);
        assertThat(card.getToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("Duct Crawler has one activated ability")
    void hasOneActivatedAbility() {
        DuctCrawler card = new DuctCrawler();
        assertThat(card.getActivatedAbilities()).hasSize(1);
    }

    // ===== Ability activation =====

    @Test
    @DisplayName("Activating ability puts it on the stack targeting an opponent's creature")
    void activatingAbilityPutsOnStack() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Duct Crawler");
        assertThat(entry.getTargetPermanentId()).isEqualTo(target.getId());
    }

    @Test
    @DisplayName("Ability does not require tapping")
    void abilityDoesNotRequireTapping() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(crawler.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Cannot activate ability without enough mana")
    void cannotActivateWithoutMana() {
        addReadyCrawler(player1);
        Permanent target = addReadyCreature(player2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    @Test
    @DisplayName("Cannot activate ability with only 1 mana instead of {1}{R}")
    void cannotActivateWithInsufficientMana() {
        addReadyCrawler(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough mana");
    }

    // ===== Ability resolution =====

    @Test
    @DisplayName("Resolving ability adds source to target's cantBlockIds")
    void resolvingAbilityAddsCantBlockRestriction() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getCantBlockIds()).contains(crawler.getId());
    }

    @Test
    @DisplayName("Ability fizzles if target is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent target = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, target.getId());

        // Remove target before resolution
        gd.playerBattlefields.get(player2.getId()).remove(target);
        gd.playerGraveyards.get(player2.getId()).add(target.getCard());

        // Should resolve without error (fizzle)
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    // ===== Blocking restrictions =====

    @Test
    @DisplayName("Targeted creature cannot block Duct Crawler after ability resolves")
    void targetedCreatureCannotBlockDuctCrawler() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Set up combat: Duct Crawler attacks
        crawler.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        // Attempting to block Duct Crawler with the targeted creature should fail
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    @Test
    @DisplayName("Targeted creature can still block other creatures")
    void targetedCreatureCanBlockOtherCreatures() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent otherAttacker = addReadyCreature(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Activate and resolve the ability targeting the blocker
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        // Set up combat: only the other creature attacks (not Duct Crawler)
        otherAttacker.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        // Blocker at index 0 blocks attacker at index 1 (otherAttacker)
        // Duct Crawler is at index 0 (not attacking), otherAttacker is at index 1
        // declareBlockers succeeds without throwing — targeted creature can block other creatures
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));
    }

    @Test
    @DisplayName("Non-targeted creature can still block Duct Crawler")
    void nonTargetedCreatureCanBlockDuctCrawler() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent targetedBlocker = addReadyCreature(player2);
        Permanent otherBlocker = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Activate and resolve the ability targeting only the first blocker
        harness.activateAbility(player1, 0, null, targetedBlocker.getId());
        harness.passBothPriorities();

        // Set up combat: Duct Crawler attacks
        crawler.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        // otherBlocker (index 1) blocks Duct Crawler (index 0)
        // declareBlockers succeeds without throwing — non-targeted creature can block Duct Crawler
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0)));
    }

    // ===== Multiple activations =====

    @Test
    @DisplayName("Can activate ability multiple times on different creatures")
    void canActivateMultipleTimes() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent blocker1 = addReadyCreature(player2);
        Permanent blocker2 = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.WHITE, 2);

        // Activate on first blocker and resolve
        harness.activateAbility(player1, 0, null, blocker1.getId());
        harness.passBothPriorities();

        // Activate on second blocker and resolve
        harness.activateAbility(player1, 0, null, blocker2.getId());
        harness.passBothPriorities();

        assertThat(blocker1.getCantBlockIds()).contains(crawler.getId());
        assertThat(blocker2.getCantBlockIds()).contains(crawler.getId());

        // Set up combat: Duct Crawler attacks
        crawler.setAttacking(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.awaitingInput = AwaitingInput.BLOCKER_DECLARATION;

        // Neither blocker can block Duct Crawler
        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't block");
    }

    // ===== End of turn reset =====

    @Test
    @DisplayName("Blocking restriction resets at end of turn")
    void restrictionResetsAtEndOfTurn() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent blocker = addReadyCreature(player2);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        // Activate and resolve the ability
        harness.activateAbility(player1, 0, null, blocker.getId());
        harness.passBothPriorities();

        assertThat(blocker.getCantBlockIds()).contains(crawler.getId());

        // Simulate end-of-turn reset (resetModifiers clears cantBlockIds)
        blocker.resetModifiers();

        assertThat(blocker.getCantBlockIds()).isEmpty();
    }

    // ===== Can activate on own creatures =====

    @Test
    @DisplayName("Can activate ability targeting own creature")
    void canTargetOwnCreature() {
        Permanent crawler = addReadyCrawler(player1);
        Permanent ownCreature = addReadyCreature(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.activateAbility(player1, 0, null, ownCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getCantBlockIds()).contains(crawler.getId());
    }

    // ===== Helper methods =====

    private Permanent addReadyCrawler(Player player) {
        DuctCrawler card = new DuctCrawler();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadyCreature(Player player) {
        GrizzlyBears card = new GrizzlyBears();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
