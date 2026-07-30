package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MaliciousIntentTest extends BaseCardTest {

    private Permanent setUpEnchantedCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        Permanent aura = new Permanent(new MaliciousIntent());
        aura.setAttachedTo(bears.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return bears;
    }

    @Test
    @DisplayName("Granted ability makes the target creature unable to block")
    void grantedAbilityMakesTargetCantBlock() {
        setUpEnchantedCreature();
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = findPermanent(player2, "Grizzly Bears").getId();

        harness.activateAbility(player1, 0, 0, null, targetId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Activating the granted ability taps the enchanted creature")
    void grantedAbilityTapsEnchantedCreature() {
        Permanent bears = setUpEnchantedCreature();
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID targetId = findPermanent(player2, "Grizzly Bears").getId();

        harness.activateAbility(player1, 0, 0, null, targetId);

        assertThat(bears.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted ability goes away when the aura leaves the battlefield")
    void grantedAbilityRemovedWhenAuraLeaves() {
        Permanent bears = setUpEnchantedCreature();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Malicious Intent"));

        assertThat(gqs.computeStaticBonus(gd, bears).grantedActivatedAbilities()).isEmpty();
    }
}
