package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.e.EndangeredArmodon;
import com.github.laxika.magicalvibes.cards.h.HonorGuard;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({YouthfulKnight.class, HonorGuard.class, EndangeredArmodon.class})
class YouthfulKnightTest extends BaseCardTest {

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack as CREATURE_SPELL")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new YouthfulKnight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new YouthfulKnight()));

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    // ===== Resolving =====

    @Test
    @DisplayName("Resolving puts Youthful Knight onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new YouthfulKnight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Youthful Knight");
    }

    @Test
    @DisplayName("Enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.setHand(player1, List.of(new YouthfulKnight()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Youthful Knight");
        assertThat(perm.isSummoningSick()).isTrue();
    }

    // ===== First strike in combat =====

    @Test
    @DisplayName("First strike kills a 1/1 before it deals regular damage")
    void firstStrikeKillsBeforeRegularDamage() {
        // Youthful Knight (2/1 first strike) attacks, blocked by a 1/1
        Permanent attacker = addCreatureReady(player1, new YouthfulKnight());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new HonorGuard());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // Youthful Knight deals 2 first strike damage → kills 1/1 before it can deal damage
        // Youthful Knight survives
        harness.assertOnBattlefield(player1, "Youthful Knight");
        harness.assertInGraveyard(player2, "Honor Guard");
    }

    @Test
    @DisplayName("Youthful Knight dies to a 4/5 blocker despite first strike")
    void diesTo4_5BlockerDespiteFirstStrike() {
        // Youthful Knight (2/1 first strike) attacks, blocked by Endangered Armodon (4/5).
        Permanent blocker = addCreatureReady(player2, new EndangeredArmodon());

        Permanent attacker = addCreatureReady(player1, new YouthfulKnight());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        // First strike deals 2, so the blocker survives; regular damage then kills Youthful Knight.
        harness.assertNotOnBattlefield(player1, "Youthful Knight");
        harness.assertInGraveyard(player1, "Youthful Knight");
        harness.assertOnBattlefield(player2, "Endangered Armodon");
    }
}

