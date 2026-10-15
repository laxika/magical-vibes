package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.e.EumidianTerrabotanist;
import com.github.laxika.magicalvibes.cards.t.TezzeretCruelCaptain;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NebulaDragon.class, EumidianTerrabotanist.class, TezzeretCruelCaptain.class})
class NebulaDragonTest extends BaseCardTest {

    @Test
    @DisplayName("When Nebula Dragon enters, it deals 3 damage to a target player")
    void enteringDealsThreeDamageToPlayer() {
        harness.setLife(player2, 20);
        castNebulaDragon(player2.getId());

        assertThat(gd.getLife(player2.getId())).isEqualTo(17);
        harness.assertOnBattlefield(player1, "Nebula Dragon");
    }

    @Test
    @DisplayName("When Nebula Dragon enters, it deals 3 damage to a target creature")
    void enteringDealsThreeDamageToCreature() {
        var target = harness.addToBattlefieldAndReturn(player2, new EumidianTerrabotanist());
        castNebulaDragon(target.getId());

        harness.assertNotOnBattlefield(player2, "Eumidian Terrabotanist");
        harness.assertInGraveyard(player2, "Eumidian Terrabotanist");
    }

    @Test
    @DisplayName("The enter trigger can damage its controller")
    void enteringCanDamageController() {
        harness.setLife(player1, 20);
        castNebulaDragon(player1.getId());

        harness.assertLife(player1, 17);
    }

    @Test
    @DisplayName("The enter trigger marks exactly three damage on a surviving creature")
    void enteringDealsNonlethalDamageToCreature() {
        var target = harness.addToBattlefieldAndReturn(player2, new NebulaDragon());
        castNebulaDragon(target.getId());

        harness.assertOnBattlefield(player2, "Nebula Dragon");
        assertThat(target.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("The enter trigger removes three loyalty from a planeswalker")
    void enteringDealsDamageToPlaneswalker() {
        var target = harness.addToBattlefieldAndReturn(player2, new TezzeretCruelCaptain());
        target.setCounterCount(CounterType.LOYALTY, 4);
        castNebulaDragon(target.getId());

        harness.assertOnBattlefield(player2, "Tezzeret, Cruel Captain");
        assertThat(target.getCounterCount(CounterType.LOYALTY)).isEqualTo(1);
    }

    @Test
    @DisplayName("The enter trigger resolves separately after the creature spell")
    void damageWaitsForTriggerResolution() {
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new NebulaDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Nebula Dragon");
        harness.assertLife(player2, 20);

        harness.passBothPriorities();

        harness.assertLife(player2, 17);
    }

    private void castNebulaDragon(java.util.UUID targetId) {
        harness.setHand(player1, List.of(new NebulaDragon()));
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
