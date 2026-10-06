package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AscendedLawmage;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScabClanGiant.class, GrizzlyBears.class, HillGiant.class, AscendedLawmage.class})
class ScabClanGiantTest extends BaseCardTest {

    @Test
    @DisplayName("Fights the only creature an opponent controls, killing it")
    void fightsSingleOpponentCreature() {
        Permanent bears = addCreature(player2, new GrizzlyBears());

        castGiantAndResolveEtb();

        // Scab-Clan Giant is 4/5: 4 damage kills the 2/2 Bears, which deal 2 back.
        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertOnBattlefield(player1, "Scab-Clan Giant");
        assertThat(giant().getMarkedDamage()).isEqualTo(2);
        assertThat(bears.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    @DisplayName("Creatures the controller owns are never fought")
    void neverFightsOwnCreature() {
        Permanent ownBears = addCreature(player1, new GrizzlyBears());
        addCreature(player2, new HillGiant());

        castGiantAndResolveEtb();

        assertThat(ownBears.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Nothing happens when no opponent controls a creature")
    void noOpponentCreatureIsNoOp() {
        Permanent ownBears = addCreature(player1, new GrizzlyBears());

        castGiantAndResolveEtb();

        harness.assertOnBattlefield(player1, "Scab-Clan Giant");
        assertThat(giant().getMarkedDamage()).isZero();
        assertThat(ownBears.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Exactly one opponent creature is fought, and which one varies across games")
    void fightsExactlyOneRandomlyChosenCreature() {
        Set<Integer> foughtSlots = new HashSet<>();

        for (int i = 0; i < 40; i++) {
            resetGame();
            Permanent first = addCreature(player2, new HillGiant());
            Permanent second = addCreature(player2, new HillGiant());

            castGiantAndResolveEtb();

            // 4 damage to a single 3/3 kills it; the survivor is untouched, and the Giant took
            // exactly one creature's 3 power back.
            boolean firstHit = !gd.playerBattlefields.get(player2.getId()).contains(first);
            boolean secondHit = !gd.playerBattlefields.get(player2.getId()).contains(second);
            assertThat(firstHit ^ secondHit).isTrue();
            assertThat(giant().getMarkedDamage()).isEqualTo(3);

            foughtSlots.add(firstHit ? 0 : 1);
        }

        assertThat(foughtSlots).hasSize(2);
    }

    @Test
    @DisplayName("Hexproof prevents an opponent's creature from being fought")
    void cannotFightHexproofCreature() {
        Permanent lawmage = harness.addToBattlefieldAndReturn(player2, new AscendedLawmage());

        castGiantAndResolveEtb();

        harness.assertOnBattlefield(player2, "Ascended Lawmage");
        assertThat(lawmage.getMarkedDamage()).isZero();
        assertThat(giant().getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("A creature entering after the trigger is stacked cannot replace its departed target")
    void doesNotChooseReplacementAfterTargetLeaves() {
        Permanent original = addCreature(player2, new GrizzlyBears());
        castGiantAndResolveSpell();
        gd.playerBattlefields.get(player2.getId()).remove(original);
        gd.playerHands.get(player2.getId()).add(original.getCard());
        Permanent newcomer = addCreature(player2, new HillGiant());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Hill Giant");
        assertThat(newcomer.getMarkedDamage()).isZero();
        assertThat(giant().getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("No fight happens if the Giant leaves before its trigger resolves")
    void noFightWhenSourceLeaves() {
        Permanent bears = addCreature(player2, new GrizzlyBears());
        castGiantAndResolveSpell();
        Permanent source = giant();
        gd.playerBattlefields.get(player1.getId()).remove(source);
        gd.playerHands.get(player1.getId()).add(source.getCard());

        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isZero();
    }

    private void resetGame() {
        gd.playerBattlefields.get(player1.getId()).clear();
        gd.playerBattlefields.get(player2.getId()).clear();
    }

    private Permanent giant() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Scab-Clan Giant"))
                .findFirst().orElseThrow();
    }

    private Permanent addCreature(Player player, Card card) {
        Permanent perm = harness.addToBattlefieldAndReturn(player, card);
        perm.setSummoningSick(false);
        return perm;
    }

    private void castGiantAndResolveEtb() {
        castGiantAndResolveSpell();
        harness.passBothPriorities();
    }

    private void castGiantAndResolveSpell() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new ScabClanGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities(); // resolve creature spell → ETB trigger
    }
}
