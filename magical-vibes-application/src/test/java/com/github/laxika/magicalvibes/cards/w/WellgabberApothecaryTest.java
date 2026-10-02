package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.d.DeeptreadMerrow;
import com.github.laxika.magicalvibes.cards.h.HillcomberGiant;
import com.github.laxika.magicalvibes.cards.k.KnightOfMeadowgrain;
import com.github.laxika.magicalvibes.cards.t.Tarfire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WellgabberApothecary.class, KnightOfMeadowgrain.class, DeeptreadMerrow.class,
        HillcomberGiant.class, Tarfire.class})
class WellgabberApothecaryTest extends BaseCardTest {

    private Permanent addTappedKnight() {
        Permanent knight = harness.addToBattlefieldAndReturn(player1, new KnightOfMeadowgrain());
        knight.tap();
        return knight;
    }

    @Test
    @DisplayName("Prevents all damage dealt to the targeted tapped Kithkin creature this turn")
    void preventsDamage() {
        harness.addToBattlefield(player1, new WellgabberApothecary());
        Permanent knight = addTappedKnight();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, knight.getId());
        harness.passBothPriorities();

        // Tarfire the protected 2/2 — all damage should be prevented, so it survives.
        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, knight.getId());

        harness.assertOnBattlefield(player1, "Knight of Meadowgrain");
        harness.assertNotInGraveyard(player1, "Knight of Meadowgrain");
        assertThat(knight.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents all damage dealt to the targeted tapped Merfolk creature this turn")
    void preventsDamageToTappedMerfolk() {
        harness.addToBattlefield(player1, new WellgabberApothecary());
        Permanent merrow = harness.addToBattlefieldAndReturn(player2, new DeeptreadMerrow());
        merrow.tap();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, merrow.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Tarfire()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveInstant(player1, 0, merrow.getId());

        harness.assertOnBattlefield(player2, "Deeptread Merrow");
        harness.assertNotInGraveyard(player2, "Deeptread Merrow");
        assertThat(merrow.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Prevents combat damage dealt to the targeted creature this turn")
    void preventsCombatDamage() {
        harness.addToBattlefield(player1, new WellgabberApothecary());
        Permanent attacker = addCreatureReady(player1, new HillcomberGiant());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());

        Permanent merrow = addCreatureReady(player2, new DeeptreadMerrow());
        merrow.tap();

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, null, merrow.getId());
        harness.passBothPriorities();

        merrow.untap();
        merrow.setBlocking(true);
        merrow.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));
        resolveCombat(player1);

        harness.assertOnBattlefield(player2, "Deeptread Merrow");
        harness.assertNotInGraveyard(player2, "Deeptread Merrow");
        assertThat(merrow.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Cannot target an untapped Kithkin creature")
    void rejectsUntappedTarget() {
        harness.addToBattlefield(player1, new WellgabberApothecary());
        harness.addToBattlefield(player1, new KnightOfMeadowgrain());
        UUID untappedKnight = harness.getPermanentId(player1, "Knight of Meadowgrain");

        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, untappedKnight))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a tapped creature that is neither Merfolk nor Kithkin")
    void rejectsWrongSubtype() {
        harness.addToBattlefield(player1, new WellgabberApothecary());
        harness.addToBattlefield(player2, new HillcomberGiant());
        Permanent giant = findPermanent(player2, "Hillcomber Giant");
        giant.tap();

        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, giant.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
