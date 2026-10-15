package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VampireNoble;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MarkovEnforcer.class, GrizzlyBears.class, VampireNoble.class})
class MarkovEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Fights an opponent creature when it enters and creates Blood when that creature dies")
    void fightsOnEntryAndCreatesBloodWhenDamagedCreatureDies() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new MarkovEnforcer());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(countPermanents(player1, "Blood")).isEqualTo(1);
    }

    @Test
    @DisplayName("Triggers when another Vampire enters")
    void fightsWhenAnotherVampireEnters() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player1, new MarkovEnforcer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new VampireNoble());
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(target);
        assertThat(enforcer.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not trigger for a non-Vampire entering")
    void doesNotTriggerForNonVampire() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player1, new MarkovEnforcer());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.enterBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
        assertThat(enforcer.getMarkedDamage()).isZero();
    }
}
