package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HazduhrTheAbbotTest extends BaseCardTest {

    private void addHazduhrReady() {
        harness.addToBattlefield(player1, new HazduhrTheAbbot());
        findPermanent(player1, "Hazduhr the Abbot").setSummoningSick(false);
    }

    private void addPyromancerReady() {
        harness.addToBattlefield(player1, new ProdigalPyromancer());
        for (Permanent perm : gd.playerBattlefields.get(player1.getId())) {
            perm.setSummoningSick(false);
        }
    }

    @Test
    @DisplayName("Activating for X registers an X-limited, any-source redirect shield protecting the target")
    void activationCreatesShield() {
        addHazduhrReady();
        harness.addToBattlefield(player1, new SerraAngel());

        UUID hazduhrId = harness.getPermanentId(player1, "Hazduhr the Abbot");
        UUID angelId = harness.getPermanentId(player1, "Serra Angel");

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, 2, angelId);
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);
        var shield = gd.creatureDamageRedirectShields.getFirst();
        assertThat(shield.protectedPermanentId()).isEqualTo(angelId);
        assertThat(shield.damageSourceId()).isNull();
        assertThat(shield.remainingAmount()).isEqualTo(2);
        assertThat(shield.redirectTargetId()).isEqualTo(hazduhrId);
    }

    @Test
    @DisplayName("Noncombat damage to the targeted white creature is dealt to Hazduhr instead")
    void redirectsNoncombatDamage() {
        addHazduhrReady();
        harness.addToBattlefield(player1, new SerraAngel());
        addPyromancerReady();

        UUID angelId = harness.getPermanentId(player1, "Serra Angel");

        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.activateAbility(player1, 0, 2, angelId);
        harness.passBothPriorities();

        // The Pyromancer pings the Angel for 1 — that damage lands on Hazduhr instead.
        harness.activateAbility(player1, 2, null, angelId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Serra Angel").getMarkedDamage()).isEqualTo(0);
        assertThat(findPermanent(player1, "Hazduhr the Abbot").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Only the paid X damage is redirected; further damage stays on the protected creature")
    void redirectsOnlyXDamage() {
        addHazduhrReady();
        harness.addToBattlefield(player1, new SerraAngel());
        addPyromancerReady();
        addPyromancerReady();

        UUID angelId = harness.getPermanentId(player1, "Serra Angel");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, 1, angelId);
        harness.passBothPriorities();

        harness.activateAbility(player1, 2, null, angelId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Hazduhr the Abbot").getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanent(player1, "Serra Angel").getMarkedDamage()).isEqualTo(0);

        // Shield spent: the second Pyromancer's ping lands on the Angel itself.
        harness.activateAbility(player1, 3, null, angelId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Serra Angel").getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanent(player1, "Hazduhr the Abbot").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Cannot target a nonwhite creature you control")
    void cannotTargetNonwhiteCreature() {
        addHazduhrReady();
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot target a white creature an opponent controls")
    void cannotTargetOpponentsWhiteCreature() {
        addHazduhrReady();
        harness.addToBattlefield(player2, new SerraAngel());

        UUID angelId = harness.getPermanentId(player2, "Serra Angel");
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 2, angelId))
                .isInstanceOf(IllegalStateException.class);
    }
}
