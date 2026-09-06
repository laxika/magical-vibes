package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalPyromancer;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DaughterOfAutumn.class, ProdigalPyromancer.class, SerraAngel.class, GrizzlyBears.class})
class DaughterOfAutumnTest extends BaseCardTest {

    private void addDaughter() {
        harness.addToBattlefield(player1, new DaughterOfAutumn());
    }

    private void addPyromancerReady() {
        addCreatureReady(player1, new ProdigalPyromancer());
    }

    @Test
    @DisplayName("Activating registers a 1-damage, any-source redirect shield pointing at Daughter of Autumn")
    void activationCreatesShield() {
        addDaughter();
        harness.addToBattlefield(player1, new SerraAngel());

        UUID daughterId = harness.getPermanentId(player1, "Daughter of Autumn");
        UUID angelId = harness.getPermanentId(player1, "Serra Angel");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, angelId);
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);
        var shield = gd.creatureDamageRedirectShields.getFirst();
        assertThat(shield.protectedPermanentId()).isEqualTo(angelId);
        assertThat(shield.damageSourceId()).isNull();
        assertThat(shield.remainingAmount()).isEqualTo(1);
        assertThat(shield.redirectTargetId()).isEqualTo(daughterId);
    }

    @Test
    @DisplayName("The next 1 damage to the targeted white creature is dealt to Daughter of Autumn instead")
    void redirectsOneDamage() {
        addDaughter();
        harness.addToBattlefield(player1, new SerraAngel());
        addPyromancerReady();
        addPyromancerReady();

        UUID angelId = harness.getPermanentId(player1, "Serra Angel");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, angelId);
        harness.passBothPriorities();

        harness.activateAbility(player1, 2, null, angelId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Serra Angel").getMarkedDamage()).isEqualTo(0);
        assertThat(findPermanent(player1, "Daughter of Autumn").getMarkedDamage()).isEqualTo(1);

        // Shield spent: the second ping lands on the Angel itself.
        harness.activateAbility(player1, 3, null, angelId);
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Serra Angel").getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanent(player1, "Daughter of Autumn").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Combat damage to the targeted white creature is dealt to Daughter of Autumn instead")
    void redirectsCombatDamage() {
        addDaughter();
        Permanent target = addCreatureReady(player1, new SerraAngel());
        addCreatureReady(player2, new GrizzlyBears());

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(1, 0)));
        resolveCombat(player2);

        assertThat(target.getMarkedDamage()).isEqualTo(1);
        assertThat(findPermanent(player1, "Daughter of Autumn").getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage is not lost when Daughter of Autumn leaves before the redirected damage")
    void doesNotLoseDamageWhenDaughterLeavesBeforeDamage() {
        addDaughter();
        Permanent target = addCreatureReady(player1, new SerraAngel());
        addPyromancerReady();

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        Permanent daughter = findPermanent(player1, "Daughter of Autumn");
        harness.inMutationScope(() -> harness.getPermanentRemovalService()
                .removePermanentToGraveyard(gd, daughter));
        harness.passBothPriorities();

        harness.activateAbility(player1, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("A white creature an opponent controls is a legal target")
    void canTargetOpponentsWhiteCreature() {
        addDaughter();
        harness.addToBattlefield(player2, new SerraAngel());

        UUID daughterId = harness.getPermanentId(player1, "Daughter of Autumn");
        UUID angelId = harness.getPermanentId(player2, "Serra Angel");

        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.activateAbility(player1, 0, null, angelId);
        harness.passBothPriorities();

        assertThat(gd.creatureDamageRedirectShields).hasSize(1);
        assertThat(gd.creatureDamageRedirectShields.getFirst().protectedPermanentId()).isEqualTo(angelId);
        assertThat(gd.creatureDamageRedirectShields.getFirst().redirectTargetId()).isEqualTo(daughterId);
    }

    @Test
    @DisplayName("Cannot target a nonwhite creature")
    void cannotTargetNonwhiteCreature() {
        addDaughter();
        harness.addToBattlefield(player1, new GrizzlyBears());

        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addMana(player1, ManaColor.WHITE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, bearsId))
                .isInstanceOf(IllegalStateException.class);
    }
}
