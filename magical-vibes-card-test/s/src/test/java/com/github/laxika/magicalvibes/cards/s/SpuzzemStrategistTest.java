package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FloralSpuzzem;
import com.github.laxika.magicalvibes.cards.f.ForethoughtAmulet;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SpuzzemStrategist.class, FloralSpuzzem.class, ForethoughtAmulet.class})
class SpuzzemStrategistTest extends BaseCardTest {

    @Test
    @DisplayName("The controller can make a choice for a Spuzzem they control")
    void controllerMakesSpuzzemChoice() {
        harness.addToBattlefield(player1, new SpuzzemStrategist());
        Permanent floralSpuzzem = addCreatureReady(player1, new FloralSpuzzem());
        floralSpuzzem.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ForethoughtAmulet());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(artifact);
    }

    @Test
    @DisplayName("The controller can decline a Spuzzem's optional destruction")
    void controllerCanDeclineSpuzzemChoice() {
        harness.addToBattlefield(player1, new SpuzzemStrategist());
        Permanent floralSpuzzem = addCreatureReady(player1, new FloralSpuzzem());
        floralSpuzzem.setAttacking(true);
        Permanent artifact = harness.addToBattlefieldAndReturn(player2, new ForethoughtAmulet());
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, artifact.getId());
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(artifact);
        harness.assertLife(player2, lifeBefore - 2);
    }

    @Test
    @DisplayName("Spuzzem Strategist can be cast as a creature")
    void castsAsCreature() {
        harness.setHand(player1, List.of(new SpuzzemStrategist()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Spuzzem Strategist");
    }
}
