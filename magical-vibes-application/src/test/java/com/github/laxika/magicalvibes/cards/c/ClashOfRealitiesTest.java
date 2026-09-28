package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ClashOfRealities.class, GoblinCohort.class, KamiOfFalseHope.class})
class ClashOfRealitiesTest extends BaseCardTest {

    @Test
    @DisplayName("An entering Spirit may deal 3 damage to a non-Spirit creature")
    void enteringSpiritShootsNonSpirit() {
        harness.addToBattlefield(player1, new ClashOfRealities());
        Permanent goblin = harness.addToBattlefieldAndReturn(player2, new GoblinCohort());

        harness.castFromHand(player1, new KamiOfFalseHope(), "{W}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(goblin.getId());

        harness.handlePermanentChosen(player1, goblin.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Goblin Cohort");
    }

    @Test
    @DisplayName("An entering non-Spirit creature may deal 3 damage to a Spirit")
    void enteringNonSpiritShootsSpirit() {
        harness.addToBattlefield(player1, new ClashOfRealities());
        Permanent kami = harness.addToBattlefieldAndReturn(player2, new KamiOfFalseHope());

        harness.castFromHand(player1, new GoblinCohort(), "{R}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(kami.getId());

        harness.handlePermanentChosen(player1, kami.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertNotOnBattlefield(player2, "Kami of False Hope");
    }

    @Test
    @DisplayName("Declining the granted trigger deals no damage")
    void decliningDealsNoDamage() {
        harness.addToBattlefield(player1, new ClashOfRealities());
        harness.addToBattlefield(player2, new GoblinCohort());

        harness.castFromHand(player1, new KamiOfFalseHope(), "{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, harness.getPermanentId(player2, "Goblin Cohort"));
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);

        harness.assertOnBattlefield(player2, "Goblin Cohort");
    }

    @Test
    @DisplayName("A Spirit entering with no non-Spirit creature around gets no trigger")
    void spiritWithoutLegalTargetGetsNoTrigger() {
        harness.addToBattlefield(player1, new ClashOfRealities());
        harness.addToBattlefield(player2, new KamiOfFalseHope());

        harness.castFromHand(player1, new KamiOfFalseHope(), "{W}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("A non-Spirit entering with no Spirit creature around gets no trigger")
    void nonSpiritWithoutLegalTargetGetsNoTrigger() {
        harness.addToBattlefield(player1, new ClashOfRealities());
        harness.addToBattlefield(player2, new GoblinCohort());

        harness.castFromHand(player1, new GoblinCohort(), "{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.pendingMayAbilities).isEmpty();
    }

    @Test
    @DisplayName("An opponent's entering non-Spirit creature gets the granted trigger")
    void opponentEnteringNonSpiritGetsTrigger() {
        harness.addToBattlefield(player1, new ClashOfRealities());
        Permanent kami = harness.addToBattlefieldAndReturn(player1, new KamiOfFalseHope());

        harness.enterBattlefieldAndReturn(player2, new GoblinCohort());
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.validIds()).containsExactly(kami.getId());

        harness.handlePermanentChosen(player2, kami.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);

        harness.assertNotOnBattlefield(player1, "Kami of False Hope");
    }
}
