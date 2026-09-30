package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardPowerToughnessModifier;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({VolatileRift.class, Forest.class, Island.class, GrizzlyBears.class})
class VolatileRiftTest extends BaseCardTest {

    @Test
    void creatureDrawnFromLibraryGetsPerpetualPowerBoostForControlledColors() {
        harness.addToBattlefield(player1, new VolatileRift());
        harness.addToBattlefield(player1, new Forest());
        harness.addToBattlefield(player1, new Island());
        Card creature = new GrizzlyBears();
        harness.setLibrary(player1, List.of(creature));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.perpetualCardPowerToughnessModifiers)
                .containsEntry(creature.getId(), new CardPowerToughnessModifier(2, 0));
    }

    @Test
    void noncreatureCardPutIntoHandDoesNotTrigger() {
        harness.addToBattlefield(player1, new VolatileRift());
        Card noncreature = new Forest();
        harness.setLibrary(player1, List.of(noncreature));

        harness.inMutationScope(() -> harness.getDrawService().resolveDrawCard(gd, player1.getId()));

        assertThat(gd.perpetualCardPowerToughnessModifiers).doesNotContainKey(noncreature.getId());
    }
}
