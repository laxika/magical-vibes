package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.c.Cancel;
import com.github.laxika.magicalvibes.cards.c.Clone;
import com.github.laxika.magicalvibes.cards.d.DarkRitual;
import com.github.laxika.magicalvibes.cards.f.FutureSight;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BramblearmorBrawler.class, Cancel.class, Clone.class, DarkRitual.class, FutureSight.class, GrizzlyBears.class})
class BramblearmorBrawlerTest extends BaseCardTest {

    @Test
    void thisSpellCannotBeCountered() {
        BramblearmorBrawler brawler = new BramblearmorBrawler();
        harness.setHand(player1, List.of(brawler));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.setHand(player2, List.of(new Cancel()));
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.forceActivePlayer(player1);
        harness.castCreature(player1, 0);
        harness.passPriority(player1);
        harness.castInstant(player2, 0, brawler.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Bramblearmor Brawler");
        harness.assertInGraveyard(player2, "Cancel");
    }

    @Test
    void opponentCastingNoncreatureSpellBoostsCreatureCardsInLibrary() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new FutureSight());
        harness.addToBattlefield(player1, new BramblearmorBrawler());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveFromLibraryTop(player1);

        Permanent enteredBears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, enteredBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, enteredBears)).isEqualTo(3);
    }

    @Test
    void opponentCastingCreatureSpellDoesNotTrigger() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addToBattlefield(player1, new FutureSight());
        harness.addToBattlefield(player1, new BramblearmorBrawler());

        GrizzlyBears bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(bears));
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveFromLibraryTop(player1);

        Permanent enteredBears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, enteredBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, enteredBears)).isEqualTo(2);
    }

    @Test
    void controllerCastingNoncreatureSpellDoesNotBoostLibrary() {
        harness.addToBattlefield(player1, new FutureSight());
        harness.addToBattlefield(player1, new BramblearmorBrawler());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new DarkRitual()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveFromLibraryTop(player1);

        Permanent bears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, bears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears)).isEqualTo(2);
    }

    @Test
    void boostedCloneKeepsPerpetualBonusWhenEnteringAsCopy() {
        harness.addToBattlefield(player1, new FutureSight());
        harness.addToBattlefield(player1, new BramblearmorBrawler());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Clone()));
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveFromLibraryTop(player1);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Grizzly Bears"));

        Permanent copiedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, copiedBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, copiedBears)).isEqualTo(3);
    }

    @Test
    void copyingBoostedCreatureDoesNotCopyItsPerpetualBonus() {
        harness.addToBattlefield(player1, new FutureSight());
        harness.addToBattlefield(player1, new BramblearmorBrawler());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);
        harness.passPriority(player1);
        harness.castInstant(player2, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveFromLibraryTop(player1);
        Permanent boostedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(gqs.getEffectivePower(gd, boostedBears)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, boostedBears)).isEqualTo(3);

        harness.setHand(player1, List.of(new Clone()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, true);
        harness.handlePermanentChosen(player1, boostedBears.getId());

        Permanent copiedBears = findPermanents(player1, "Grizzly Bears").get(1);
        assertThat(gqs.getEffectivePower(gd, copiedBears)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, copiedBears)).isEqualTo(2);
    }

    @Test
    void repeatedTriggersBoostAllLibraryCreaturesButNotCreaturesInHand() {
        harness.addToBattlefield(player1, new FutureSight());
        harness.addToBattlefield(player1, new BramblearmorBrawler());
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new DarkRitual(), new DarkRitual()));
        harness.addMana(player2, ManaColor.BLACK, 1);

        harness.ensurePriority(player2);
        harness.castInstant(player2, 0);
        resolveAllTriggers();
        harness.ensurePriority(player2);
        harness.castInstant(player2, 0);
        resolveAllTriggers();

        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castAndResolveFromLibraryTop(player1);
        harness.castAndResolveFromLibraryTop(player1);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        List<Permanent> bears = findPermanents(player1, "Grizzly Bears");
        assertThat(bears).hasSize(3);
        assertThat(gqs.getEffectivePower(gd, bears.get(0))).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears.get(0))).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears.get(1))).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, bears.get(1))).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, bears.get(2))).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, bears.get(2))).isEqualTo(2);
    }
}
