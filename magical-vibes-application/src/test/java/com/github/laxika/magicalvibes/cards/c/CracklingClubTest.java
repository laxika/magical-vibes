package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.FountainOfYouth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CracklingClub.class, GrizzlyBears.class, FountainOfYouth.class})
class CracklingClubTest extends BaseCardTest {

    @Test
    @DisplayName("Enchanted creature gets +1/+0")
    void enchantedCreatureGetsBoost() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent club = harness.addToBattlefieldAndReturn(player1, new CracklingClub());
        club.setAttachedTo(bears.getId());

        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    @DisplayName("Sacrificing Crackling Club deals 1 damage to target creature")
    void sacrificeDealsDamageAndPutsClubInGraveyard() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent club = harness.addToBattlefieldAndReturn(player1, new CracklingClub());
        club.setAttachedTo(bears.getId());

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getMarkedDamage()).isEqualTo(1);
        harness.assertInGraveyard(player1, "Crackling Club");
    }

    @Test
    @DisplayName("Crackling Club cannot target a noncreature permanent")
    void cannotTargetNonCreature() {
        Permanent fountain = harness.addToBattlefieldAndReturn(player2, new FountainOfYouth());
        harness.addToBattlefield(player1, new CracklingClub());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, fountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }
}
