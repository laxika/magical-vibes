package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.p.PhantasmalTerrain;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({IslandFishJasconius.class, Island.class, Forest.class, PhantasmalTerrain.class})
class IslandFishJasconiusTest extends BaseCardTest {

    @Test
    @DisplayName("Sacrificed when controller controls no Islands")
    void sacrificedWhenNoIslands() {
        harness.castFromHand(player1, new IslandFishJasconius(), "{4}{U}{U}{U}");
        resolveAllTriggers();

        harness.assertNotOnBattlefield(player1, "Island Fish Jasconius");
        harness.assertInGraveyard(player1, "Island Fish Jasconius");
    }

    @Test
    @DisplayName("Survives while controller controls an Island")
    void survivesWithIsland() {
        harness.addToBattlefield(player1, new Island());
        harness.castFromHand(player1, new IslandFishJasconius(), "{4}{U}{U}{U}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Island Fish Jasconius");
    }

    @Test
    @DisplayName("Survives while a land is currently an Island")
    void survivesWithLandCurrentlyAnIsland() {
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        Permanent terrain = harness.addToBattlefieldAndReturn(player1, new PhantasmalTerrain());
        terrain.setAttachedTo(forest.getId());
        terrain.setChosenSubtype(CardSubtype.ISLAND);
        Permanent fish = addCreatureReady(player1, new IslandFishJasconius());

        assertThat(gqs.effectiveBasicLandTypes(gd, forest)).contains(CardSubtype.ISLAND);

        harness.runStateBasedActions();
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fish);
    }

    @Test
    @DisplayName("Tapped Island Fish Jasconius does not untap during controller's untap step")
    void doesNotUntapDuringUntapStep() {
        harness.addToBattlefield(player1, new Island());
        Permanent fish = addCreatureReady(player1, new IslandFishJasconius());
        fish.tap();

        harness.performUntapStep(player1);

        assertThat(fish.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Paying {U}{U}{U} during upkeep untaps Island Fish Jasconius")
    void payingUntapsFish() {
        harness.addToBattlefield(player1, new Island());
        Permanent fish = addCreatureReady(player1, new IslandFishJasconius());
        fish.tap();

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passBothPriorities(); // resolve MayPayManaEffect from stack
        harness.handleMayAbilityChosen(player1, true);

        assertThat(fish.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining the upkeep payment leaves Island Fish Jasconius tapped")
    void decliningLeavesFishTapped() {
        harness.addToBattlefield(player1, new Island());
        Permanent fish = addCreatureReady(player1, new IslandFishJasconius());
        fish.tap();

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.BLUE, 3);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(fish.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Can attack when defending player controls an Island")
    void canAttackWhenDefenderControlsIsland() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Island());
        harness.addToBattlefield(player2, new Island());

        Permanent fish = addCreatureReady(player1, new IslandFishJasconius());

        int fishIndex = gd.playerBattlefields.get(player1.getId()).indexOf(fish);
        declareAttackers(player1, List.of(fishIndex));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(14);
    }

    @Test
    @DisplayName("Cannot attack when defending player controls no Island")
    void cannotAttackWhenDefenderHasNoIsland() {
        harness.addToBattlefield(player1, new Island());

        Permanent fish = addCreatureReady(player1, new IslandFishJasconius());

        int fishIndex = gd.playerBattlefields.get(player1.getId()).indexOf(fish);
        assertThatThrownBy(() -> declareAttackers(player1, List.of(fishIndex)))
                .isInstanceOf(IllegalStateException.class);
    }

}
