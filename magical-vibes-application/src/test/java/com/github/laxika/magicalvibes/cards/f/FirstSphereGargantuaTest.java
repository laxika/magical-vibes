package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.s.SnowCoveredForest;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({FirstSphereGargantua.class, SnowCoveredForest.class})
class FirstSphereGargantuaTest extends BaseCardTest {

    @Test
    @DisplayName("ETB draws a card and loses 1 life")
    void etbDrawsAndLosesLife() {
        harness.setHand(player1, List.of(new FirstSphereGargantua()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.setLibrary(player1, List.of(new SnowCoveredForest()));

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        harness.assertInHand(player1, "Snow-Covered Forest");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 1);
    }

    @Test
    @DisplayName("Unearth returns First-Sphere Gargantua with haste and exiles it at the next end step")
    void unearthReturnsAndExilesAtEndStep() {
        harness.setGraveyard(player1, List.of(new FirstSphereGargantua()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent gargantua = findPermanent(player1, "First-Sphere Gargantua");
        assertThat(gargantua.getGrantedKeywords()).contains(Keyword.HASTE);

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player1, "First-Sphere Gargantua");
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(cardInExile -> cardInExile.getName().equals("First-Sphere Gargantua"));
    }

    @Test
    @DisplayName("Unearth triggers the draw and life loss even if the creature leaves before the trigger resolves")
    void unearthTriggerResolvesAfterCreatureLeaves() {
        FirstSphereGargantua card = new FirstSphereGargantua();
        harness.setGraveyard(player1, List.of(card));
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new SnowCoveredForest()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.activateGraveyardAbility(player1, 0);
        harness.passBothPriorities();

        Permanent gargantua = findPermanent(player1, "First-Sphere Gargantua");
        gargantua.setMarkedDamage(4);
        harness.runStateBasedActions();

        harness.assertNotOnBattlefield(player1, "First-Sphere Gargantua");
        harness.assertNotInGraveyard(player1, "First-Sphere Gargantua");
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);

        harness.passBothPriorities();

        harness.assertInHand(player1, "Snow-Covered Forest");
        harness.assertLife(player1, 19);
        harness.assertLife(player2, 20);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Unearth cannot be activated outside a main phase")
    void unearthCannotBeActivatedDuringUpkeep() {
        harness.setGraveyard(player1, List.of(new FirstSphereGargantua()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.forceStep(TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        harness.assertInGraveyard(player1, "First-Sphere Gargantua");
        harness.assertNotOnBattlefield(player1, "First-Sphere Gargantua");
    }

    @Test
    @DisplayName("Unearth cannot be activated while another unearth ability is on the stack")
    void unearthRequiresEmptyStack() {
        harness.setGraveyard(player1, List.of(new FirstSphereGargantua(), new FirstSphereGargantua()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.BLACK, 2);

        harness.activateGraveyardAbility(player1, 0);

        assertThatThrownBy(() -> harness.activateGraveyardAbility(player1, 1))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
    }
}
