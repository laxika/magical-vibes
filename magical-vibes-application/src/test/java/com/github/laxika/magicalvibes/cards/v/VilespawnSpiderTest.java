package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class VilespawnSpiderTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger mills a card")
    void upkeepTriggerMillsACard() {
        harness.addToBattlefield(player1, new VilespawnSpider());
        int graveyardSizeBefore = gd.playerGraveyards.get(player1.getId()).size();
        int librarySizeBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(graveyardSizeBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySizeBefore - 1);
    }

    @Test
    @DisplayName("Ability creates an Insect for each creature card in the graveyard, counting the sacrificed Spider")
    void abilityCreatesInsectPerCreatureCardInGraveyard() {
        addCreatureReady(player1, new VilespawnSpider());
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LightningBolt()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Vilespawn Spider");
        harness.assertInGraveyard(player1, "Vilespawn Spider");
        assertThat(countPermanents(player1, "Insect")).isEqualTo(2);
    }

    @Test
    @DisplayName("Ability cannot be activated outside a main phase")
    void abilityIsSorcerySpeedOnly() {
        addCreatureReady(player1, new VilespawnSpider());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UPKEEP);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }
}
