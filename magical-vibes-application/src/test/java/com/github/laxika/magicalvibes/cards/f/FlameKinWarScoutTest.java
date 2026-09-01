package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.c.ColossalDreadmaw;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FlameKinWarScout.class, GrizzlyBears.class, ColossalDreadmaw.class})
class FlameKinWarScoutTest extends BaseCardTest {

    @Test
    void anotherCreatureEnteringSacrificesScoutAndDealsDamageToIt() {
        harness.addToBattlefield(player1, new FlameKinWarScout());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Flame-Kin War Scout");
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    void enteringCreatureSurvivesWithFourMarkedDamage() {
        harness.addToBattlefield(player1, new FlameKinWarScout());
        harness.setHand(player1, List.of(new ColossalDreadmaw()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent dreadmaw = findPermanent(player1, "Colossal Dreadmaw");
        assertThat(dreadmaw.getMarkedDamage()).isEqualTo(4);
        harness.assertInGraveyard(player1, "Flame-Kin War Scout");
    }

    @Test
    void doesNotTriggerForItsOwnEntry() {
        harness.addToBattlefield(player1, new FlameKinWarScout());

        harness.assertOnBattlefield(player1, "Flame-Kin War Scout");
    }
}
