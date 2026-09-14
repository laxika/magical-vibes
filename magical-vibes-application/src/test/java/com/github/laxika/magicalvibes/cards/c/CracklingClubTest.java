package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.SengirVampire;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CracklingClub.class, SengirVampire.class, CabalCoffers.class})
class CracklingClubTest extends BaseCardTest {

    @Test
    @DisplayName("Crackling Club can enchant a creature and grants it +1/+0")
    void enchantedCreatureGetsBoost() {
        Permanent vampire = addCreatureReady(player2, new SengirVampire());
        harness.setHand(player1, List.of(new CracklingClub()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castEnchantment(player1, 0, vampire.getId());
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, vampire)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, vampire)).isEqualTo(4);
        assertThat(findPermanent(player1, "Crackling Club").getAttachedTo()).isEqualTo(vampire.getId());
    }

    @Test
    @DisplayName("Sacrificing Crackling Club deals 1 damage to target creature")
    void sacrificeDealsDamageAndPutsClubInGraveyard() {
        Permanent vampire = addCreatureReady(player2, new SengirVampire());
        Permanent club = harness.addToBattlefieldAndReturn(player1, new CracklingClub());
        club.setAttachedTo(vampire.getId());

        harness.activateAbility(player1, 0, null, vampire.getId());

        assertThat(vampire.getMarkedDamage()).isZero();
        harness.assertNotOnBattlefield(player1, "Crackling Club");
        harness.assertInGraveyard(player1, "Crackling Club");
        harness.passBothPriorities();

        assertThat(vampire.getMarkedDamage()).isEqualTo(1);
        harness.assertOnBattlefield(player2, "Sengir Vampire");
    }

    @Test
    @DisplayName("Crackling Club cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent coffers = harness.addToBattlefieldAndReturn(player2, new CabalCoffers());
        harness.addToBattlefield(player1, new CracklingClub());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, coffers.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
