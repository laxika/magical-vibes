package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpawningPod.class, GrizzlyBears.class, LlanowarElves.class})
class SpawningPodTest extends BaseCardTest {

    @Test
    void sacrificesCreatureAndSeeksCreatureWithOneHigherManaValue() {
        harness.addToBattlefield(player1, new SpawningPod());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        Permanent sought = findPermanent(player1, "Grizzly Bears");
        assertThat(sought.getGrantedSubtypes()).contains(CardSubtype.PHYREXIAN);
    }

    @Test
    void doesNotPutCreatureOntoBattlefieldWhenManaValueDoesNotMatch() {
        harness.addToBattlefield(player1, new SpawningPod());
        harness.addToBattlefield(player1, new LlanowarElves());
        harness.setLibrary(player1, List.of(new LlanowarElves()));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Llanowar Elves");
        harness.assertNotOnBattlefield(player1, "Llanowar Elves");
    }
}
