package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CustodyBattle.class, Forest.class, GrizzlyBears.class})
class CustodyBattleTest extends BaseCardTest {

    @Test
    @DisplayName("The enchanted creature's controller chooses an opponent for the upkeep trigger")
    void targetsOpponentOfEnchantedCreatureController() {
        Permanent bears = addBattle();

        advanceToUpkeep(player1);

        PendingInteraction.PermanentChoice choice =
                (PendingInteraction.PermanentChoice) gd.interaction.activeInteraction();
        assertThat(choice.playerId()).isEqualTo(player1.getId());
        assertThat(choice.validIds()).containsExactly(player2.getId());

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears);
    }

    @Test
    @DisplayName("Sacrificing a land keeps control of the enchanted creature")
    void sacrificingLandPreventsControlChange() {
        Permanent bears = addBattle();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(bears).doesNotContain(forest);
        harness.assertInGraveyard(player1, "Forest");
    }

    @Test
    @DisplayName("Declining the land sacrifice gives the enchanted creature to the target opponent")
    void decliningSacrificeChangesControl() {
        Permanent bears = addBattle();
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(bears).contains(forest);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(bears);
    }

    @Test
    @DisplayName("The ability does not trigger during the Aura controller's upkeep")
    void doesNotTriggerDuringAuraControllersUpkeep() {
        addBattle();

        advanceToUpkeep(player2);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addBattle() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent aura = harness.addToBattlefieldAndReturn(player2, new CustodyBattle());
        aura.setAttachedTo(bears.getId());
        return bears;
    }
}
