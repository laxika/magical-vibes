package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Hivestone.class, GrizzlyBears.class})
class HivestoneTest extends BaseCardTest {

    @Test
    @DisplayName("Creatures you control are Slivers in addition to their other types")
    void grantsSliverToOwnCreatures() {
        harness.addToBattlefield(player1, new Hivestone());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        Permanent ownBear = findPermanent(player1, "Grizzly Bears");
        Permanent opposingBear = findPermanent(player2, "Grizzly Bears");

        assertThat(gqs.effectiveCreatureSubtypes(gd, ownBear))
                .contains(CardSubtype.BEAR, CardSubtype.SLIVER);
        assertThat(gqs.effectiveCreatureSubtypes(gd, opposingBear))
                .containsExactly(CardSubtype.BEAR);
    }

    @Test
    @DisplayName("The subtype grant applies to creatures entering after Hivestone")
    void appliesToCreaturesEnteringLater() {
        harness.addToBattlefield(player1, new Hivestone());
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bear = findPermanent(player1, "Grizzly Bears");

        assertThat(gqs.effectiveCreatureSubtypes(gd, bear))
                .contains(CardSubtype.BEAR, CardSubtype.SLIVER);
    }
}
