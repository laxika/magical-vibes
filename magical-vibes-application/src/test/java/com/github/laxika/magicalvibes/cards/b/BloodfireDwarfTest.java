package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodfireDwarf.class, GrizzlyBears.class, SuntailHawk.class})
class BloodfireDwarfTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificing it deals 1 damage to each creature without flying")
    void sacrificesItAndDamagesOnlyCreaturesWithoutFlying() {
        Permanent dwarf = harness.addToBattlefieldAndReturn(player1, new BloodfireDwarf());
        Permanent groundCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent flyingCreature = harness.addToBattlefieldAndReturn(player2, new SuntailHawk());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Bloodfire Dwarf");
        assertThat(groundCreature.getMarkedDamage()).isEqualTo(1);
        assertThat(flyingCreature.getMarkedDamage()).isZero();
        assertThat(dwarf).isNotIn(gd.playerBattlefields.get(player1.getId()));
    }
}
