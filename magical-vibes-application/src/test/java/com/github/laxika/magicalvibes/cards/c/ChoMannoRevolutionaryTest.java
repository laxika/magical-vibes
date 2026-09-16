package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.l.Lunge;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ChoMannoRevolutionary.class, CrossbowInfantry.class, Lunge.class})
class ChoMannoRevolutionaryTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts Cho-Manno on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new ChoMannoRevolutionary(), "{2}{W}{W}");

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Cho-Manno, Revolutionary");
    }

    @Test
    @DisplayName("Resolving puts Cho-Manno onto the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.castFromHand(player1, new ChoMannoRevolutionary(), "{2}{W}{W}");
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Cho-Manno, Revolutionary");
    }

    @Test
    @DisplayName("Cannot cast without enough mana")
    void cannotCastWithoutEnoughMana() {
        harness.setHand(player1, List.of(new ChoMannoRevolutionary()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("not playable");
    }

    @Test
    @DisplayName("Cho-Manno survives combat against a larger creature")
    void survivesCombatAgainstALargerCreature() {
        Permanent blocker = addCreatureReady(player2, new ChoMannoRevolutionary());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        CrossbowInfantry bigCreature = new CrossbowInfantry();
        bigCreature.setPower(5);
        bigCreature.setToughness(5);
        Permanent attacker = addCreatureReady(player1, bigCreature);
        attacker.setAttacking(true);

        resolveCombat(player1);

        // Cho-Manno survives — all damage prevented
        harness.assertOnBattlefield(player2, "Cho-Manno, Revolutionary");
        // Attacker takes 2 damage from Cho-Manno (2 < 5 toughness) → survives too
        harness.assertOnBattlefield(player1, "Crossbow Infantry");
    }

    @Test
    @DisplayName("Cho-Manno still deals combat damage to blockers")
    void stillDealsCombatDamage() {
        // Cho-Manno (2/2, prevent all) attacks, blocked by Crossbow Infantry (1/1)
        Permanent attacker = addCreatureReady(player1, new ChoMannoRevolutionary());
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new CrossbowInfantry());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat(player1);

        // Cho-Manno survives (all damage prevented)
        harness.assertOnBattlefield(player1, "Cho-Manno, Revolutionary");
        // Crossbow Infantry dies (took 2 damage, toughness 1)
        harness.assertNotOnBattlefield(player2, "Crossbow Infantry");
        harness.assertInGraveyard(player2, "Crossbow Infantry");
    }

    @Test
    @DisplayName("Cho-Manno deals combat damage to player when unblocked")
    void dealsDamageToPlayerWhenUnblocked() {
        harness.setLife(player2, 20);

        Permanent attacker = addCreatureReady(player1, new ChoMannoRevolutionary());
        attacker.setAttacking(true);

        resolveCombat(player1);

        assertThat(harness.getGameData().playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Cho-Manno prevents noncombat damage from a spell")
    void preventsNoncombatSpellDamage() {
        Permanent choManno = addCreatureReady(player1, new ChoMannoRevolutionary());
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new Lunge()));
        harness.addMana(player1, ManaColor.RED, 3);

        harness.castInstant(player1, 0, List.of(choManno.getId(), player2.getId()));
        harness.passBothPriorities();

        assertThat(choManno.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Cho-Manno, Revolutionary");
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevention is not consumed — Cho-Manno survives multiple combats")
    void preventionNotConsumed() {
        Permanent defender = addCreatureReady(player2, new ChoMannoRevolutionary());

        Permanent attacker = addCreatureReady(player1, new CrossbowInfantry());

        // First combat
        attacker.setAttacking(true);
        defender.setBlocking(true);
        defender.addBlockingTarget(0);
        resolveCombat(player1);

        // Cho-Manno survived, Crossbow Infantry died
        harness.assertOnBattlefield(player2, "Cho-Manno, Revolutionary");

        // Add a new attacker for second combat
        CrossbowInfantry largerAttackerCard = new CrossbowInfantry();
        largerAttackerCard.setPower(4);
        largerAttackerCard.setToughness(4);
        Permanent attacker2 = addCreatureReady(player1, largerAttackerCard);
        attacker2.setAttacking(true);

        defender.setBlocking(true);
        defender.addBlockingTarget(0);
        resolveCombat(player1);

        // Cho-Manno still survives second combat
        harness.assertOnBattlefield(player2, "Cho-Manno, Revolutionary");
    }

    @Test
    @DisplayName("Cho-Manno enters battlefield with summoning sickness")
    void entersBattlefieldWithSummoningSickness() {
        harness.castFromHand(player1, new ChoMannoRevolutionary(), "{2}{W}{W}");
        harness.passBothPriorities();

        Permanent perm = findPermanent(player1, "Cho-Manno, Revolutionary");
        assertThat(perm.isSummoningSick()).isTrue();
    }
}

