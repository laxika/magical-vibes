package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.b.BriarbridgeTracker;
import com.github.laxika.magicalvibes.cards.b.BristlebudFarmer;
import com.github.laxika.magicalvibes.cards.f.ForswornPaladin;
import com.github.laxika.magicalvibes.cards.g.GildedGoose;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.cards.s.SecondHarvest;
import com.github.laxika.magicalvibes.cards.w.WilyGoblin;
import com.github.laxika.magicalvibes.cards.x.Xorn;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;




@CardUsed({AcademyManufactor.class, BristlebudFarmer.class, ForswornPaladin.class,
        NoviceInspector.class, WilyGoblin.class, GildedGoose.class, BriarbridgeTracker.class,
        SecondHarvest.class, Xorn.class})
class AcademyManufactorTest extends BaseCardTest {

    @Test
    void replacesFoodCreationWithEachTokenType() {
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.setHand(player1, List.of(new BristlebudFarmer()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

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
        resolveAllTriggers();

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
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isEqualTo(3);
        assertThat(countPermanents(player1, "Clue")).isEqualTo(3);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(3);
    }
    @Test
    void replacesTreasureWithOneOfEachToken() {
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.setHand(player1, List.of(new WilyGoblin()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

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
        resolveAllTriggers();

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
        resolveAllTriggers();

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
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Treasure")).hasSize(3);
        assertThat(findPermanents(player1, "Clue")).hasSize(3);
        assertThat(findPermanents(player1, "Food")).hasSize(3);
    }

    @Test
    void doesNotReplaceTokensCreatedByOpponent() {
        harness.addToBattlefield(player2, new AcademyManufactor());
        harness.setHand(player1, List.of(new GildedGoose()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player1, "Clue")).isZero();
        assertThat(countPermanents(player1, "Treasure")).isZero();
        assertThat(countPermanents(player2, "Food")).isZero();
        assertThat(countPermanents(player2, "Clue")).isZero();
        assertThat(countPermanents(player2, "Treasure")).isZero();
    }

    @Test
    void threeManufactorsCreateNineOfEachToken() {
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.setHand(player1, List.of(new GildedGoose()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isEqualTo(9);
        assertThat(countPermanents(player1, "Clue")).isEqualTo(9);
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(9);
    }

    @Test
    void replacesCopyOfFoodTokenWithOneOfEach() {
        harness.setHand(player1, List.of(new GildedGoose(), new SecondHarvest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(countPermanents(player1, "Food")).isEqualTo(2);
        assertThat(countPermanents(player1, "Clue")).isOne();
        assertThat(countPermanents(player1, "Treasure")).isOne();
    }

    @Test
    void xornAddsTreasureToManufactorReplacementOfFood() {
        harness.addToBattlefield(player1, new AcademyManufactor());
        harness.addToBattlefield(player1, new Xorn());
        harness.setHand(player1, List.of(new GildedGoose()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(countPermanents(player1, "Food")).isOne();
        assertThat(countPermanents(player1, "Clue")).isOne();
        assertThat(countPermanents(player1, "Treasure")).isEqualTo(2);
    }

}
