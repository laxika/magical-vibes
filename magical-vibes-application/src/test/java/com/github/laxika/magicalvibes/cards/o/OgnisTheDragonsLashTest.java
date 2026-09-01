package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({OgnisTheDragonsLash.class, GrizzlyBears.class})
class OgnisTheDragonsLashTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking with a creature with haste creates a tapped Treasure")
    void hasteCreatureAttacksCreatesTappedTreasure() {
        addCreatureReady(player1, new OgnisTheDragonsLash());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        List<Permanent> treasures = findPermanents(player1, "Treasure");
        assertThat(treasures).hasSize(1);
        assertThat(treasures.getFirst().isTapped()).isTrue();
    }

    @Test
    @DisplayName("Attacking with a creature without haste does not create a Treasure")
    void nonHasteCreatureAttacksDoesNotCreateTreasure() {
        addCreatureReady(player1, new OgnisTheDragonsLash());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).isEmpty();
    }

    @Test
    @DisplayName("Only attacking creatures with haste create Treasures")
    void mixedAttackersOnlyHasteCreatureCreatesTreasure() {
        addCreatureReady(player1, new OgnisTheDragonsLash());
        addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
    }
}
