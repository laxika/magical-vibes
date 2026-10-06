package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AmyPond;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RoryWilliams.class, AmyPond.class, ReverseThePolarity.class})
class RoryWilliamsTest extends BaseCardTest {

    @Test
    void investigatesEvenWhenRoryIsCounteredBeforeHisCastTriggerResolves() {
        RoryWilliams rory = new RoryWilliams();
        harness.setHand(player1, List.of(rory));
        harness.setHand(player2, List.of(new ReverseThePolarity()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.BLUE, 3);

        harness.castCreature(player1, 0, player2.getId());
        harness.passPriority(player1);
        harness.castInstant(player2, 0, 0, null);
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Rory Williams");
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.getPlayerExiledCards(player1.getId())).doesNotContain(rory);
        assertThat(gd.suspendedSpellExiles).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingFromExileDoesNotPutTheLastCenturionTriggerOnTheStack() {
        RoryWilliams rory = new RoryWilliams();
        harness.setExile(player1, List.of(rory));
        gd.exilePlayPermissions.put(rory.getId(), player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFromExile(player1, rory.getId(), player2.getId());

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    void partnerCanSearchForAmyInItsControllersLibraryWithoutBeingCast() {
        AmyPond amy = new AmyPond();
        harness.setLibrary(player1, List.of(amy));

        harness.enterBattlefieldAndReturn(player1, new RoryWilliams());
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).contains(amy);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.suspendedSpellExiles).isEmpty();
    }

    @Test
    void partnerWithAmyLetsTargetPlayerSearchTheirLibrary() {
        AmyPond amy = new AmyPond();
        harness.setLibrary(player2, List.of(amy));
        RoryWilliams rory = new RoryWilliams();
        harness.setExile(player1, List.of(rory));
        gd.exilePlayPermissions.put(rory.getId(), player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castFromExile(player1, rory.getId(), player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        harness.handleCardChosen(player2, 0);

        assertThat(gd.playerHands.get(player2.getId())).contains(amy);
    }

    @Test
    void castingFromHandExilesRoryWithSuspendAndInvestigates() {
        RoryWilliams rory = new RoryWilliams();
        harness.setHand(player1, List.of(rory));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castCreature(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(rory);
        assertThat(gd.suspendedSpellExiles).contains(
                new com.github.laxika.magicalvibes.model.GameData.SuspendedSpellExile(
                        rory.getId(), player1.getId(), 3));
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void castingFromExileDoesNotExileAgainOrInvestigate() {
        RoryWilliams rory = new RoryWilliams();
        harness.setExile(player1, List.of(rory));
        gd.exilePlayPermissions.put(rory.getId(), player1.getId());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castFromExile(player1, rory.getId(), player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);

        assertThat(findPermanent(player1, "Rory Williams")).isNotNull();
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(gd.exiledCardTimeCounters).doesNotContainKey(rory.getId());
    }
}
