package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class HostileRealmTest extends BaseCardTest {

    private Permanent setUpEnchantedForest() {
        harness.addToBattlefield(player1, new Forest());
        Permanent forest = gd.playerBattlefields.get(player1.getId()).getFirst();
        Permanent aura = new Permanent(new HostileRealm());
        aura.setAttachedTo(forest.getId());
        gd.playerBattlefields.get(player1.getId()).add(aura);
        return forest;
    }

    @Test
    @DisplayName("Enchanted land's granted ability makes target creature can't block")
    void grantedAbilityMakesTargetCantBlock() {
        setUpEnchantedForest();
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = findPermanent(player2, "Grizzly Bears").getId();

        // Activate the granted ability on the forest (ability index 0)
        harness.activateAbility(player1, 0, 0, null, bearsId);
        harness.passBothPriorities();

        assertThat(findPermanent(player2, "Grizzly Bears").isCantBlockThisTurn()).isTrue();
    }

    @Test
    @DisplayName("Activating the granted ability taps the enchanted land")
    void grantedAbilityTapsLand() {
        Permanent forest = setUpEnchantedForest();
        harness.addToBattlefield(player2, new GrizzlyBears());
        UUID bearsId = findPermanent(player2, "Grizzly Bears").getId();

        harness.activateAbility(player1, 0, 0, null, bearsId);

        assertThat(forest.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Granted ability goes away when aura leaves the battlefield")
    void grantedAbilityRemovedWhenAuraLeaves() {
        Permanent forest = setUpEnchantedForest();

        gd.playerBattlefields.get(player1.getId())
                .removeIf(p -> p.getCard().getName().equals("Hostile Realm"));

        assertThat(gqs.computeStaticBonus(gd, forest).grantedActivatedAbilities()).isEmpty();
    }
}
