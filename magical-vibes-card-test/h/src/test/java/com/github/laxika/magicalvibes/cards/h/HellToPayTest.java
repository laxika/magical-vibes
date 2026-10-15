package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.o.OasisGardener;
import com.github.laxika.magicalvibes.cards.e.EverlastingTorment;
import com.github.laxika.magicalvibes.cards.g.GideonBlackblade;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HellToPay.class, OasisGardener.class, EverlastingTorment.class, GideonBlackblade.class})
class HellToPayTest extends BaseCardTest {

    @Test
    @DisplayName("Deals X damage and creates tapped Treasures for excess damage")
    void createsTappedTreasuresEqualToExcessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OasisGardener());

        harness.setHand(player1, List.of(new HellToPay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castAndResolveSorcery(player1, 0, 5, target.getId());

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(3);
        assertThat(treasures).allMatch(Permanent::isTapped);
        harness.assertNotOnBattlefield(player2, "Oasis Gardener");
    }

    @Test
    @DisplayName("Creates no Treasures when the damage is not excess")
    void createsNoTreasuresWithoutExcessDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OasisGardener());

        harness.setHand(player1, List.of(new HellToPay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("A creature planeswalker uses the greatest excess damage among its types")
    void creaturePlaneswalkerCountsExcessBeyondLoyalty() {
        harness.addToBattlefield(player1, new EverlastingTorment());
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GideonBlackblade());
        target.setCounterCount(CounterType.LOYALTY, 1);
        harness.setHand(player1, List.of(new HellToPay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 2, target.getId());

        assertThat(findPermanents(player1, "Treasure")).hasSize(1).allMatch(Permanent::isTapped);
        harness.assertInGraveyard(player1, "Gideon Blackblade");
    }

    @Test
    @DisplayName("X equal to zero deals no damage and creates no Treasures")
    void zeroDamageCreatesNoTreasures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OasisGardener());
        harness.setHand(player1, List.of(new HellToPay()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveSorcery(player1, 0, 0, target.getId());

        assertThat(target.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Oasis Gardener");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Sublethal damage to your own creature creates no Treasures")
    void sublethalDamageToOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new OasisGardener());
        harness.setHand(player1, List.of(new HellToPay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castAndResolveSorcery(player1, 0, 1, target.getId());

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Oasis Gardener");
        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Damage already marked increases the excess damage")
    void markedDamageCountsTowardLethalDamage() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OasisGardener());
        harness.setHand(player1, List.of(new HellToPay(), new HellToPay()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castAndResolveSorcery(player1, 0, 1, target.getId());
        harness.castAndResolveSorcery(player1, 0, 3, target.getId());

        assertThat(findPermanents(player1, "Treasure")).hasSize(2).allMatch(Permanent::isTapped);
        assertThat(findPermanents(player2, "Treasure")).isEmpty();
        harness.assertInGraveyard(player2, "Oasis Gardener");
    }

    @Test
    @DisplayName("An absent target prevents both damage and Treasure creation")
    void invalidTargetCreatesNoTreasures() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new OasisGardener());
        harness.setHand(player1, List.of(new HellToPay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.castSorcery(player1, 0, 5, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
        harness.assertInGraveyard(player1, "Hell to Pay");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Rejects a noncreature target")
    void rejectsNonCreatureTarget() {
        harness.setHand(player1, List.of(new HellToPay()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
