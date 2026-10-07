package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.ThoseWhoServe;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SupplyCaravan.class, ThoseWhoServe.class})
class SupplyCaravanTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a 1/1 white Warrior with vigilance when you control a tapped creature")
    void createsWarriorWhenControllingTappedCreature() {
        Permanent tappedCreature = addCreatureReady(player1, new ThoseWhoServe());
        tappedCreature.tap();

        harness.setHand(player1, List.of(new SupplyCaravan()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        Permanent token = findPermanent(player1, "Warrior");
        assertThat(token.getCard().getPower()).isEqualTo(1);
        assertThat(token.getCard().getToughness()).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, token, Keyword.VIGILANCE)).isTrue();
    }

    @Test
    @DisplayName("Creates no token when you control no tapped creature")
    void createsNoTokenWithoutTappedCreature() {
        addCreatureReady(player1, new ThoseWhoServe());

        harness.setHand(player1, List.of(new SupplyCaravan()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Warrior");
    }

    @Test
    @DisplayName("Does not trigger when only the opponent controls a tapped creature")
    void doesNotTriggerForOpponentsTappedCreature() {
        addCreatureReady(player2, new ThoseWhoServe()).tap();
        harness.setHand(player1, List.of(new SupplyCaravan()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Warrior");
        harness.assertNotOnBattlefield(player2, "Warrior");
    }

    @Test
    @DisplayName("Tapping a creature after entry cannot cause the ability to trigger")
    void doesNotTriggerRetroactively() {
        Permanent creature = addCreatureReady(player1, new ThoseWhoServe());
        harness.setHand(player1, List.of(new SupplyCaravan()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        creature.tap();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Warrior");
    }

    @Test
    @DisplayName("Creates no token if the only tapped creature untaps before resolution")
    void rechecksTappedCreatureOnResolution() {
        Permanent creature = addCreatureReady(player1, new ThoseWhoServe());
        creature.tap();
        harness.setHand(player1, List.of(new SupplyCaravan()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        creature.untap();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertNotOnBattlefield(player1, "Warrior");
    }

    @Test
    @DisplayName("A different tapped creature can satisfy the condition on resolution")
    void createsTokenWhenDifferentCreatureIsTappedOnResolution() {
        Permanent original = addCreatureReady(player1, new ThoseWhoServe());
        original.tap();
        Permanent replacement = addCreatureReady(player1, new ThoseWhoServe());
        harness.setHand(player1, List.of(new SupplyCaravan()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).hasSize(1);
        original.untap();
        replacement.tap();
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(countPermanents(player1, "Warrior")).isEqualTo(1);
        harness.assertNotOnBattlefield(player2, "Warrior");
    }
}
