package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NantukoHusk;
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

@CardUsed({WorkshopWarchief.class, GrizzlyBears.class, NantukoHusk.class})
class WorkshopWarchiefTest extends BaseCardTest {

    @Test
    @DisplayName("Normal cast gains life and creates a Rhino Warrior on death")
    void normalCastGainsLifeAndCreatesTokenOnDeath() {
        harness.addToBattlefield(player1, new NantukoHusk());
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new WorkshopWarchief()));
        harness.addMana(player1, ManaColor.GREEN, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        Permanent warchief = findPermanent(player1, "Workshop Warchief");
        assertThat(gqs.hasKeyword(gd, warchief, Keyword.HASTE)).isFalse();

        harness.activateAbility(player1, 0, null, null);
        harness.handlePermanentChosen(player1, warchief.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Workshop Warchief");
        assertThat(findPermanents(player1, "Rhino Warrior")).hasSize(1);
    }

    @Test
    @DisplayName("Blitz grants haste, gains life, draws on death, and creates a Rhino Warrior")
    void blitzGrantsHasteGainsLifeDrawsAndCreatesToken() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new WorkshopWarchief()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreatureWithAlternateCost(player1, 0, List.of());
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent warchief = findPermanent(player1, "Workshop Warchief");
        assertThat(gd.getLife(player1.getId())).isEqualTo(13);
        assertThat(gqs.hasKeyword(gd, warchief, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(warchief);
        harness.passBothPriorities();
        harness.passBothPriorities();
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Workshop Warchief");
        harness.assertInHand(player1, "Grizzly Bears");
        assertThat(findPermanents(player1, "Rhino Warrior")).hasSize(1);
    }
}
