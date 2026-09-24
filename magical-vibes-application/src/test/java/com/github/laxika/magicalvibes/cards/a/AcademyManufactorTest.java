package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BristlebudFarmer;
import com.github.laxika.magicalvibes.cards.f.ForswornPaladin;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AcademyManufactor.class, BristlebudFarmer.class, ForswornPaladin.class,
        NoviceInspector.class})
class AcademyManufactorTest extends BaseCardTest {

    @Test
    void replacesFoodCreationWithEachTokenType() {
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.setHand(player1, List.of(new BristlebudFarmer()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
        assertThat(countPermanents(player1, "Clue")).isEqualTo(2);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

    @Test
    void replacesTreasureCreationWithEachTokenType() {
        harness.addToBattlefield(player1, new AcademyManufactor());
        addCreatureReady(player1, new ForswornPaladin());
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateAbility(player1, 1, 0, null, null);
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player1, "Clue")).isOne();
        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    void replacesClueCreationWithEachTokenType() {
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player1, "Clue")).isOne();
        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    void multipleManufactorsMultiplyEachTokenTypeByThreePerCopy() {
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.setHand(player1, List.of(new NoviceInspector()));
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Food")).isEqualTo(3);
        assertThat(countPermanents(player1, "Clue")).isEqualTo(3);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
    }
}
