package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DakmorSorceress.class, Swamp.class, Plains.class})
class DakmorSorceressTest extends BaseCardTest {

    @Test
    @DisplayName("Power equals the number of Swamps you control; toughness stays 4")
    void powerEqualsControlledSwamps() {
        Permanent sorceress = addCreatureReady(player1, new DakmorSorceress());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Plains());

        assertThat(gqs.getEffectivePower(gd, sorceress)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, sorceress)).isEqualTo(4);
    }

    @Test
    @DisplayName("Counts only your Swamps, not opponent Swamps")
    void countsOnlyControllersSwamps() {
        Permanent sorceress = addCreatureReady(player1, new DakmorSorceress());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, sorceress)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, sorceress)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power updates as Swamps change; toughness remains 4")
    void powerUpdatesWhenSwampsChange() {
        Permanent sorceress = addCreatureReady(player1, new DakmorSorceress());

        assertThat(gqs.getEffectivePower(gd, sorceress)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, sorceress)).isEqualTo(4);

        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        assertThat(gqs.getEffectivePower(gd, sorceress)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, sorceress)).isEqualTo(4);

        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard().getName().equals("Swamp"));
        assertThat(gqs.getEffectivePower(gd, sorceress)).isEqualTo(0);
        assertThat(gqs.getEffectiveToughness(gd, sorceress)).isEqualTo(4);
    }

    @Test
    @DisplayName("Power is defined in hand and graveyard using the owner's Swamps")
    void powerIsDefinedOutsideBattlefield() {
        DakmorSorceress inHand = new DakmorSorceress();
        DakmorSorceress inGraveyard = new DakmorSorceress();
        harness.setHand(player1, List.of(inHand));
        harness.setGraveyard(player1, List.of(inGraveyard));
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isZero();
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isZero();

        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player1, new Swamp());
        assertThat(gqs.getEffectiveCardPower(gd, inHand)).isEqualTo(2);
        assertThat(gqs.getEffectiveCardPower(gd, inGraveyard)).isEqualTo(2);
    }

    @Test
    @DisplayName("Power follows the current controller rather than the owner")
    void powerUpdatesWhenControllerChanges() {
        DakmorSorceress card = new DakmorSorceress();
        card.setOwnerId(player1.getId());
        Permanent sorceress = harness.addToBattlefieldAndReturn(player1, card);
        harness.addToBattlefield(player1, new Swamp());
        harness.addToBattlefield(player2, new Swamp());
        harness.addToBattlefield(player2, new Swamp());

        assertThat(gqs.getEffectivePower(gd, sorceress)).isEqualTo(1);

        gd.playerBattlefields.get(player1.getId()).remove(sorceress);
        gd.playerBattlefields.get(player2.getId()).add(sorceress);

        assertThat(gqs.getEffectivePower(gd, sorceress)).isEqualTo(2);
    }
}
