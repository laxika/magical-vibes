package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.b.BoggartBirthRite;
import com.github.laxika.magicalvibes.cards.b.BoggartHarbinger;
import com.github.laxika.magicalvibes.cards.c.CaterwaulingBoggart;
import com.github.laxika.magicalvibes.cards.l.LeafGilder;
import com.github.laxika.magicalvibes.model.BlockerAssignment;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({WortBoggartAuntie.class, CaterwaulingBoggart.class, BoggartHarbinger.class,
        LeafGilder.class, BoggartBirthRite.class, WoodlandChangeling.class})
class WortBoggartAuntieTest extends BaseCardTest {

    @Test
    @DisplayName("Controller's upkeep returns Goblin from graveyard to hand")
    void upkeepReturnsGoblinFromGraveyardToHand() {
        harness.addToBattlefield(player1, new WortBoggartAuntie());
        harness.setGraveyard(player1, List.of(new CaterwaulingBoggart()));

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        harness.handleMultipleCardsChosen(player1,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Caterwauling Boggart");
        harness.assertNotInGraveyard(player1, "Caterwauling Boggart");
    }

    @Test
    @DisplayName("Returns specific Goblin when multiple Goblins are in graveyard")
    void returnsSpecificGoblinFromGraveyard() {
        harness.addToBattlefield(player1, new WortBoggartAuntie());
        harness.setGraveyard(player1, List.of(new CaterwaulingBoggart(), new BoggartHarbinger()));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1,
                List.of(gd.playerGraveyards.get(player1.getId()).get(1).getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Boggart Harbinger");
        harness.assertInGraveyard(player1, "Caterwauling Boggart");
        harness.assertNotInGraveyard(player1, "Boggart Harbinger");
    }

    @Test
    @DisplayName("No graveyard choice when only non-Goblin cards are present")
    void noEffectWithOnlyNonGoblinsInGraveyard() {
        harness.addToBattlefield(player1, new WortBoggartAuntie());
        harness.setGraveyard(player1, List.of(new LeafGilder()));

        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Leaf Gilder");
    }

    @Test
    @DisplayName("Upkeep trigger does NOT fire during opponent's upkeep")
    void upkeepTriggerDoesNotFireDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new WortBoggartAuntie());
        harness.setGraveyard(player1, List.of(new CaterwaulingBoggart()));

        advanceToUpkeep(player2);

        harness.assertInGraveyard(player1, "Caterwauling Boggart");
    }

    @Test
    void mayDeclineReturnAfterChoosingTarget() {
        harness.addToBattlefield(player1, new WortBoggartAuntie());
        harness.setGraveyard(player1, List.of(new CaterwaulingBoggart()));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInGraveyard(player1, "Caterwauling Boggart");
        harness.assertNotInHand(player1, "Caterwauling Boggart");
    }

    @Test
    void returnsNoncreatureGoblinCard() {
        harness.addToBattlefield(player1, new WortBoggartAuntie());
        harness.setGraveyard(player1, List.of(new BoggartBirthRite()));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Boggart Birth Rite");
        harness.assertNotInGraveyard(player1, "Boggart Birth Rite");
    }

    @Test
    void returnsChangelingCard() {
        harness.addToBattlefield(player1, new WortBoggartAuntie());
        harness.setGraveyard(player1, List.of(new WoodlandChangeling()));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1,
                List.of(gd.playerGraveyards.get(player1.getId()).getFirst().getId()));
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);

        harness.assertInHand(player1, "Woodland Changeling");
        harness.assertNotInGraveyard(player1, "Woodland Changeling");
    }

    @Test
    void noTargetFromOpponentsGraveyard() {
        harness.addToBattlefield(player1, new WortBoggartAuntie());
        harness.setGraveyard(player2, List.of(new CaterwaulingBoggart()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player2, "Caterwauling Boggart");
    }

    @Test
    void fearPreventsNonblackNonartifactBlocker() {
        addCreatureReady(player1, new WortBoggartAuntie());
        addCreatureReady(player2, new LeafGilder());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("fear");
    }

    @Test
    void fearAllowsBlackBlocker() {
        addCreatureReady(player1, new WortBoggartAuntie());
        var blocker = addCreatureReady(player2, new BoggartHarbinger());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void noTriggerOnStackWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new WortBoggartAuntie());
        harness.setGraveyard(player1, List.of());

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    void doesNotRetargetWhenChosenCardLeavesGraveyardBeforeResolution() {
        harness.addToBattlefield(player1, new WortBoggartAuntie());
        var target = new CaterwaulingBoggart();
        var otherGoblin = new BoggartHarbinger();
        harness.setGraveyard(player1, List.of(target, otherGoblin));

        advanceToUpkeep(player1);
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.setGraveyard(player1, List.of(otherGoblin));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Boggart Harbinger");
        harness.assertNotInHand(player1, "Boggart Harbinger");
        harness.assertNotInHand(player1, "Caterwauling Boggart");
    }
}
