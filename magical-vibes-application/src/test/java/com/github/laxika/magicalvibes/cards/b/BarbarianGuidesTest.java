package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScatheZombies;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSupertype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.EnumSet;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BarbarianGuidesTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature can't be blocked while the defender controls a snow land of the chosen type")
    void grantsSnowLandwalkOfChosenType() {
        addSnowLand(player2, new Swamp());
        Permanent blocker = readyCreature(player2, new GrizzlyBears());
        Permanent attacker = readyAttacker(player1);

        grantSnowLandwalk(attacker, "SWAMP");

        beginBlockers();
        assertThatThrownBy(() -> declareBlock(blocker, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can't be blocked");
    }

    @Test
    @DisplayName("A non-snow land of the chosen type does not turn the landwalk on")
    void nonSnowLandDoesNotGrantEvasion() {
        harness.addToBattlefield(player2, new Swamp());
        Permanent blocker = readyCreature(player2, new GrizzlyBears());
        Permanent attacker = readyAttacker(player1);
        harness.setLife(player2, 20);

        grantSnowLandwalk(attacker, "SWAMP");

        beginBlockers();
        declareBlock(blocker, attacker);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A snow land of a different type does not turn the landwalk on")
    void snowLandOfOtherTypeDoesNotGrantEvasion() {
        addSnowLand(player2, new Forest());
        Permanent blocker = readyCreature(player2, new GrizzlyBears());
        Permanent attacker = readyAttacker(player1);
        harness.setLife(player2, 20);

        grantSnowLandwalk(attacker, "SWAMP");

        beginBlockers();
        declareBlock(blocker, attacker);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("The snow-landwalking attacker is not offered as a blockable attacker")
    void snowLandwalkerIsNotOfferedAsBlockableAttacker() {
        addSnowLand(player2, new Swamp());
        readyCreature(player2, new GrizzlyBears());
        Permanent landwalker = readyAttacker(player1);
        Permanent plainAttacker = readyCreature(player1, new ScatheZombies());

        grantSnowLandwalk(landwalker, "SWAMP");
        plainAttacker.setAttacking(true);

        // The prompt path (which attackers the defender may be asked to block) must agree with the
        // enforcement path: only the attacker without snow swampwalk is offered.
        List<Integer> blockable = harness.getCombatBlockService()
                .getBlockableAttackerIndices(gd, player1.getId(), player2.getId());

        assertThat(blockable)
                .contains(indexOf(player1, plainAttacker))
                .doesNotContain(indexOf(player1, landwalker));
    }

    @Test
    @DisplayName("The target creature is returned to its owner's hand at the beginning of the next end step")
    void returnsTargetToHandAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        Permanent guides = readyCreature(player1, new BarbarianGuides());
        Permanent bears = readyCreature(player1, new GrizzlyBears());

        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, indexOf(player1, guides), 0, null, bears.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, "SWAMP");

        harness.assertOnBattlefield(player1, "Grizzly Bears");

        // Advance to the end step — the creature is bounced to its owner's hand.
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Cannot target an opponent's creature")
    void cannotTargetOpponentCreature() {
        Permanent guides = readyCreature(player1, new BarbarianGuides());
        Permanent opponentBears = readyCreature(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.RED, 3);
        assertThatThrownBy(() -> harness.activateAbility(
                player1, indexOf(player1, guides), 0, null, opponentBears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    /**
     * Activates the ability targeting {@code attacker}, answers the land-type prompt, and flags the
     * creature as attacking. Resolving the ability advances the turn out of combat, so the attacking
     * flag is set afterwards and {@link #beginBlockers()} forces the step back.
     */
    private void grantSnowLandwalk(Permanent attacker, String landType) {
        Permanent guides = readyCreature(player1, new BarbarianGuides());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.addMana(player1, ManaColor.RED, 3);
        harness.activateAbility(player1, indexOf(player1, guides), 0, null, attacker.getId());
        harness.passBothPriorities();
        harness.handleListChoice(player1, landType);

        attacker.setAttacking(true);
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                indexOf(player2, blocker), indexOf(player1, attacker))));
    }

    private int indexOf(Player player, Permanent permanent) {
        return gd.playerBattlefields.get(player.getId()).indexOf(permanent);
    }

    /** The creature that will attack; {@link #grantSnowLandwalk} flags it as attacking. */
    private Permanent readyAttacker(Player player) {
        return readyCreature(player, new GrizzlyBears());
    }

    private Permanent readyCreature(Player player, Card card) {
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void addSnowLand(Player player, Card land) {
        Permanent snowLand = new Permanent(land);
        TestCards.mutableCard(snowLand).setSupertypes(EnumSet.of(CardSupertype.BASIC, CardSupertype.SNOW));
        gd.playerBattlefields.get(player.getId()).add(snowLand);
    }

    private void beginBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
