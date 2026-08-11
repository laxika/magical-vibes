package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.v.VolcanicGeyser;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DevouringStrossusTest extends BaseCardTest {

    @Test
    @DisplayName("At the beginning of upkeep, sacrifices a creature")
    void sacrificesCreatureAtBeginningOfUpkeep() {
        harness.addToBattlefield(player1, new DevouringStrossus());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Devouring Strossus");
        harness.assertInGraveyard(player1, "Devouring Strossus");
    }

    @Test
    @DisplayName("Sacrificing a creature grants regeneration and survives lethal damage")
    void sacrificingCreatureRegeneratesAndSurvivesLethalDamage() {
        Permanent strossus = harness.addToBattlefieldAndReturn(player1, new DevouringStrossus());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(strossus.getRegenerationShield()).isEqualTo(1);

        harness.setHand(player1, List.of(new VolcanicGeyser()));
        harness.addMana(player1, ManaColor.RED, 11);
        harness.castInstant(player1, 0, 9, strossus.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(strossus);
        assertThat(strossus.isTapped()).isTrue();
        assertThat(strossus.getRegenerationShield()).isZero();
    }
}
