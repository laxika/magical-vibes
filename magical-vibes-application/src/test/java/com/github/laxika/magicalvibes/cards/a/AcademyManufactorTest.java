package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BriarbridgeTracker;
import com.github.laxika.magicalvibes.cards.g.GildedGoose;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcademyManufactor.class, WilyGoblin.class, GildedGoose.class, BriarbridgeTracker.class})
class AcademyManufactorTest extends BaseCardTest {

    @Test
    void replacesTreasureWithOneOfEachToken() {
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.setHand(player1, List.of(new WilyGoblin()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    void replacesFoodWithOneOfEachToken() {
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.setHand(player1, List.of(new GildedGoose()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    void replacesClueWithOneOfEachToken() {
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.setHand(player1, List.of(new BriarbridgeTracker()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Food")).hasSize(1);
    }

    @Test
    void multipleManufactorsCompoundTheReplacement() {
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.setHand(player1, List.of(new WilyGoblin()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
        assertThat(findPermanents(player1, "Clue")).hasSize(3);
        assertThat(findPermanents(player1, "Food")).hasSize(3);
    }
}
