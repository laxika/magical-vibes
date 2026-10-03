package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlmightyBrushwagg;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ChitteringHarvester.class, AlmightyBrushwagg.class})
class ChitteringHarvesterTest extends BaseCardTest {

    @Test
    @DisplayName("Mutating makes each opponent sacrifice a creature")
    void mutatingMakesEachOpponentSacrificeCreature() {
        Permanent harvester = addCreatureReady(player1, new ChitteringHarvester());
        addCreatureReady(player1, new AlmightyBrushwagg());
        addCreatureReady(player2, new AlmightyBrushwagg());

        triggerMutation(harvester);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Almighty Brushwagg");
        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");
        harness.assertInGraveyard(player2, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("Each opponent chooses which creature to sacrifice")
    void opponentChoosesCreatureToSacrifice() {
        Permanent harvester = addCreatureReady(player1, new ChitteringHarvester());
        Permanent first = addCreatureReady(player2, new AlmightyBrushwagg());
        Permanent second = addCreatureReady(player2, new AlmightyBrushwagg());

        triggerMutation(harvester);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player2.getId());
        assertThat(choice.validPermanentIds()).containsExactlyInAnyOrder(first.getId(), second.getId());

        harness.handlePermanentChosen(player2, second.getId());

        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(permanent -> permanent.getId().equals(first.getId()));
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(permanent -> permanent.getId().equals(second.getId()));
    }

    @Test
    @DisplayName("Casting normally does not cause an opponent to sacrifice")
    void normalCastDoesNotTriggerSacrifice() {
        addCreatureReady(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new ChitteringHarvester()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Chittering Harvester");
        harness.assertOnBattlefield(player2, "Almighty Brushwagg");
        harness.assertNotInGraveyard(player2, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("Mutate can be cast for four generic and one black mana")
    void mutateCostMergesAndTriggersSacrifice() {
        Permanent host = addCreatureReady(player1, new AlmightyBrushwagg());
        addCreatureReady(player2, new AlmightyBrushwagg());
        harness.setHand(player1, List.of(new ChitteringHarvester()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castWithAlternateCost(player1, 0, host.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(1);
        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");
        harness.assertInGraveyard(player2, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("An opponent without creatures has nothing to sacrifice")
    void opponentWithoutCreaturesDoesNotPrompt() {
        Permanent harvester = addCreatureReady(player1, new ChitteringHarvester());

        triggerMutation(harvester);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Chittering Harvester");
    }

    @Test
    @DisplayName("The sacrifice trigger resolves after its source leaves the battlefield")
    void triggerResolvesWithoutSource() {
        Permanent harvester = addCreatureReady(player1, new ChitteringHarvester());
        addCreatureReady(player2, new AlmightyBrushwagg());

        triggerMutation(harvester);
        gd.playerBattlefields.get(player1.getId()).remove(harvester);
        gd.playerGraveyards.get(player1.getId()).add(harvester.getCard());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Almighty Brushwagg");
        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");
    }

    @Test
    @DisplayName("Each subsequent mutation causes another sacrifice")
    void subsequentMutationTriggersAgain() {
        Permanent harvester = addCreatureReady(player1, new ChitteringHarvester());
        addCreatureReady(player2, new AlmightyBrushwagg());

        triggerMutation(harvester);
        harness.passBothPriorities();
        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");

        addCreatureReady(player2, new AlmightyBrushwagg());
        triggerMutation(harvester);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Almighty Brushwagg");
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .filteredOn(card -> card.getName().equals("Almighty Brushwagg"))
                .hasSize(2);
    }

    private void triggerMutation(Permanent harvester) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, harvester, List.of(harvester.getCard()), player1.getId()));
    }
}
