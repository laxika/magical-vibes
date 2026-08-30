package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TimeWipe.class, GiantSpider.class, GrizzlyBears.class, HillGiant.class})
class TimeWipeTest extends BaseCardTest {

    @Test
    @DisplayName("Returns a chosen creature you control before destroying all remaining creatures")
    void returnsChosenCreatureThenDestroysAllOtherCreatures() {
        addCreatureReady(player1, new GrizzlyBears());
        UUID bearsId = harness.getPermanentId(player1, "Grizzly Bears");
        addCreatureReady(player1, new HillGiant());
        addCreatureReady(player2, new GiantSpider());

        castTimeWipe();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(bearsId,
                harness.getPermanentId(player1, "Hill Giant"));
        assertThat(choice.validPermanentIds()).doesNotContain(harness.getPermanentId(player2, "Giant Spider"));

        harness.handlePermanentChosen(player1, bearsId);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Hill Giant");
        harness.assertNotOnBattlefield(player2, "Giant Spider");
    }

    @Test
    @DisplayName("Destroys all creatures when you control none to return")
    void destroysAllCreaturesWhenNoCreatureCanBeReturned() {
        addCreatureReady(player2, new GiantSpider());

        castTimeWipe();

        harness.assertNotOnBattlefield(player2, "Giant Spider");
    }

    private void castTimeWipe() {
        harness.setHand(player1, java.util.List.of(new TimeWipe()));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }
}
