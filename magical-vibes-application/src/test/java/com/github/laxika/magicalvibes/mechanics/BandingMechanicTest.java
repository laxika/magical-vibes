package com.github.laxika.magicalvibes.mechanics;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Banding (CR 702.22): band declaration (702.22c/d), shared blocking (702.22h), and the two
 * combat-damage-assignment reversals (702.22j — a banding blocker lets the defending player assign
 * the attacker's damage; 702.22k — a blocker blocking a banding attacker lets the active player
 * assign the blocker's damage).
 */
@Tag("scryfall")
class BandingMechanicTest {

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

    private static Card creature(String name, int power, int toughness, Keyword... keywords) {
        Card c = new Card();
        c.setName(name);
        c.setType(CardType.CREATURE);
        c.setManaCost("{1}");
        c.setPower(power);
        c.setToughness(toughness);
        EnumSet<Keyword> kws = EnumSet.noneOf(Keyword.class);
        kws.addAll(List.of(keywords));
        c.setKeywords(kws);
        return c;
    }

    private List<Permanent> atkBf() {
        return gd.playerBattlefields.get(player1.getId());
    }

    private List<Permanent> defBf() {
        return gd.playerBattlefields.get(player2.getId());
    }

    // ---- CR 702.22j: banding while blocking ----

    @Test
    @DisplayName("702.22j: a banding blocker lets the defending player assign the attacker's damage")
    void bandingBlockerLetsDefenderAssignAttackerDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, creature("Charging Bear", 2, 2));
        harness.addToBattlefield(player2, creature("Banding Wall", 1, 1, Keyword.BANDING));
        harness.addToBattlefield(player2, creature("Plain Wall", 1, 1));

        Permanent attacker = atkBf().get(0);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);

        Permanent bandingBlocker = defBf().get(0);
        Permanent plainBlocker = defBf().get(1);
        bandingBlocker.setBlocking(true);
        bandingBlocker.addBlockingTarget(0);
        plainBlocker.setBlocking(true);
        plainBlocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // CR 702.22j — the defending player, not the active player, assigns the attacker's damage.
        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player2.getId());

        // The active player is not allowed to make this assignment.
        assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(bandingBlocker.getId(), 2)))
                .isInstanceOf(IllegalStateException.class);

        // The defender pushes all of the attacker's damage onto the plain wall, sparing the banding one.
        harness.handleCombatDamageAssigned(player2, 0, Map.of(plainBlocker.getId(), 2));

        assertThat(defBf()).anyMatch(p -> p.getCard().getName().equals("Banding Wall"));
        assertThat(defBf()).noneMatch(p -> p.getCard().getName().equals("Plain Wall"));
        // The 2/2 attacker took 1+1 from the two walls and died.
        assertThat(atkBf()).noneMatch(p -> p.getCard().getName().equals("Charging Bear"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Two non-banding blockers: the active player still assigns (baseline, CR 510.1c)")
    void withoutBandingActivePlayerAssigns() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, creature("Charging Bear", 2, 2));
        harness.addToBattlefield(player2, creature("Plain Wall A", 1, 1));
        harness.addToBattlefield(player2, creature("Plain Wall B", 1, 1));

        Permanent attacker = atkBf().get(0);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        defBf().get(0).setBlocking(true);
        defBf().get(0).addBlockingTarget(0);
        defBf().get(1).setBlocking(true);
        defBf().get(1).addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
    }

    // ---- CR 702.22k: banding while attacking ----

    @Test
    @DisplayName("702.22k: a blocker blocking a banding attacker has its damage divided by the active player")
    void blockingBandingAttackerLetsActivePlayerDivideBlockerDamage() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, creature("Banded Ox", 2, 2, Keyword.BANDING));
        harness.addToBattlefield(player1, creature("Plain Ox", 2, 2));
        harness.addToBattlefield(player2, creature("Great Wall", 3, 6));

        UUID band = UUID.randomUUID();
        Permanent bandingAtk = atkBf().get(0);
        Permanent plainAtk = atkBf().get(1);
        bandingAtk.setSummoningSick(false);
        bandingAtk.setAttacking(true);
        bandingAtk.setBandId(band);
        plainAtk.setSummoningSick(false);
        plainAtk.setAttacking(true);
        plainAtk.setBandId(band);

        // The blocker is blocking the whole band (as CR 702.22h would produce).
        Permanent blocker = defBf().get(0);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        blocker.addBlockingTarget(1);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // CR 702.22k — the active player, not the defending player, divides the blocker's damage.
        PendingInteraction.CombatDamageAssignment prompt =
                gd.interaction.activeInteraction(PendingInteraction.CombatDamageAssignment.class);
        assertThat(prompt).isNotNull();
        assertThat(prompt.playerId()).isEqualTo(player1.getId());
        assertThat(prompt.totalDamage()).isEqualTo(3);

        // The active player funnels the blocker's 3 damage onto the plain attacker, sparing the banded one.
        harness.handleCombatDamageAssigned(player1, 0, Map.of(plainAtk.getId(), 3));

        assertThat(atkBf()).anyMatch(p -> p.getCard().getName().equals("Banded Ox"));
        assertThat(atkBf()).noneMatch(p -> p.getCard().getName().equals("Plain Ox"));
        // The 3/6 wall took 2+2 and survives; nothing got through to the player.
        assertThat(defBf()).anyMatch(p -> p.getCard().getName().equals("Great Wall"));
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    // ---- CR 702.22h: shared blocking ----

    @Test
    @DisplayName("702.22h: blocking one band member blocks the whole band, even an evasive member")
    void blockingOneBandMemberBlocksTheWholeBand() {
        harness.addToBattlefield(player1, creature("Banding Hawk", 2, 2, Keyword.BANDING, Keyword.FLYING));
        harness.addToBattlefield(player1, creature("Foot Soldier", 2, 2));
        harness.addToBattlefield(player2, creature("Pikeman", 1, 4));

        Permanent flyer = atkBf().get(0);
        Permanent ground = atkBf().get(1);
        flyer.setSummoningSick(false);
        ground.setSummoningSick(false);

        // Declare the two attackers as a band via the real declare-attackers flow.
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.getGameService().declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1)));

        assertThat(flyer.getBandId()).isNotNull();
        assertThat(flyer.getBandId()).isEqualTo(ground.getBandId());

        // The ground blocker can only legally block the ground attacker (index 1)...
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
        harness.getGameService().declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        // ...but CR 702.22h makes it block the flying band-mate too.
        Permanent blocker = defBf().get(0);
        assertThat(blocker.getBlockingTargetIds()).contains(flyer.getId(), ground.getId());
    }

    // ---- CR 702.22c/d: band declaration ----

    @Test
    @DisplayName("702.22c: a valid band (one banding + one non-banding) is accepted and shares a band id")
    void validBandIsAccepted() {
        harness.addToBattlefield(player1, creature("Banded Ox", 2, 2, Keyword.BANDING));
        harness.addToBattlefield(player1, creature("Plain Ox", 2, 2));

        beginAttack();
        // Call the sub-service directly so the declaration doesn't auto-advance combat (with no
        // blockers, the whole combat would resolve and clearCombatState would wipe the band id).
        harness.getCombatAttackService().declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1)));

        assertThat(atkBf().get(0).getBandId()).isNotNull();
        assertThat(atkBf().get(0).getBandId()).isEqualTo(atkBf().get(1).getBandId());
    }

    @Test
    @DisplayName("702.22c: a band with two non-banding creatures is rejected")
    void bandWithTwoNonBandingRejected() {
        harness.addToBattlefield(player1, creature("Banded Ox", 2, 2, Keyword.BANDING));
        harness.addToBattlefield(player1, creature("Plain Ox A", 2, 2));
        harness.addToBattlefield(player1, creature("Plain Ox B", 2, 2));

        beginAttack();
        assertThatThrownBy(() -> harness.getGameService().declareAttackers(
                gd, player1, List.of(0, 1, 2), null, List.of(List.of(0, 1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("without banding");
    }

    @Test
    @DisplayName("702.22c: a band with no banding creature is rejected")
    void bandWithoutBandingRejected() {
        harness.addToBattlefield(player1, creature("Plain Ox A", 2, 2));
        harness.addToBattlefield(player1, creature("Plain Ox B", 2, 2));

        beginAttack();
        assertThatThrownBy(() -> harness.getGameService().declareAttackers(
                gd, player1, List.of(0, 1), null, List.of(List.of(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("with banding");
    }

    @Test
    @DisplayName("702.22c: a creature can't be a member of two bands")
    void creatureCannotBeInTwoBands() {
        harness.addToBattlefield(player1, creature("Banded Ox A", 2, 2, Keyword.BANDING));
        harness.addToBattlefield(player1, creature("Banded Ox B", 2, 2, Keyword.BANDING));
        harness.addToBattlefield(player1, creature("Plain Ox", 2, 2));

        beginAttack();
        assertThatThrownBy(() -> harness.getGameService().declareAttackers(
                gd, player1, List.of(0, 1, 2), null, List.of(List.of(0, 2), List.of(1, 2))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("more than one band");
    }

    private void beginAttack() {
        for (Permanent p : atkBf()) {
            p.setSummoningSick(false);
        }
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
    }
}
