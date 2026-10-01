package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SilentExtraction.class, GrizzlyBears.class, GiantGrowth.class})
class SilentExtractionTest extends BaseCardTest {

    @Test
    @DisplayName("Counters the target spell and seeks a matching creature with threshold")
    void countersAndSeeksWithThreshold() {
        GrizzlyBears target = new GrizzlyBears();
        GrizzlyBears sought = new GrizzlyBears();
        GiantGrowth differentManaValue = new GiantGrowth();

        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.setHand(player2, List.of(new SilentExtraction()));
        harness.setGraveyard(player2, List.of(
                new GiantGrowth(), new GiantGrowth(), new GiantGrowth(), new GiantGrowth(),
                new GiantGrowth(), new GiantGrowth(), new GiantGrowth()));
        harness.setLibrary(player2, List.of(differentManaValue, sought));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(gd.playerHands.get(player2.getId())).contains(sought);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(differentManaValue);
    }

    @Test
    @DisplayName("Lets the target spell resolve when its controller pays")
    void paymentLetsSpellResolve() {
        GrizzlyBears target = new GrizzlyBears();
        GrizzlyBears sought = new GrizzlyBears();

        harness.setHand(player1, List.of(target));
        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.setHand(player2, List.of(new SilentExtraction()));
        harness.setLibrary(player2, List.of(sought));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Grizzly Bears");
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(sought);
    }
}
